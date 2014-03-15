package de.fusionfactory.index_vivus.services.scalaimpl

import org.scalatest.FlatSpec
import de.fusionfactory.index_vivus.services.Language
import de.fusionfactory.index_vivus.models.IDictionaryEntry
import java.util.{List => JList}
import scala.collection.convert.wrapAll._
import org.scalamock.scalatest.MockFactory

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
class FullTextSearchServiceTest extends FlatSpec with MockFactory {

  def mockedService(results: Int) =  new FullTextSearchServiceImpl with IndexSearchProvider {

    val searchMock = mock[IndexSearch]
    val resultMock = mock[DictionaryEntryListWithTotalCount]


    toMockFunction0(resultMock.list _).stubs().returning(List.fill[IDictionaryEntry](results)(mock[IDictionaryEntry]))
    toMockFunction0(resultMock.total _).stubs().returning(results).anyNumberOfTimes

    (searchMock.getSearchResults _).expects("der", Language.LATIN, 10, 0).returning(resultMock).anyNumberOfTimes()

    override def indexer: IndexSearch = searchMock
  }


  "The fulltext search service" should "allow for empty search results" in {
    val result = mockedService(0).getMatches("der", Language.LATIN, 1, 10)
    result.totalHits === 0
    result.hits.isEmpty
    (result.hasPreviousPage, result.hasNextPage) === (false, false)
  }

  it should "provide correct pagination for 7 results on page limit 10" in {
    val result = mockedService(7).getMatches("der", Language.LATIN, 1, 10)
    result.totalHits === 7
    result.hits.size() === 7
    (result.hasPreviousPage, result.hasNextPage) === (false, false)
  }

  it should "provide correct pagination for 10 results on page limit 10" in {
    val result = mockedService(10).getMatches("der", Language.LATIN, 1, 10)
    result.totalHits === 10
    result.hits.size() === 10
    (result.hasPreviousPage, result.hasNextPage) === (false, false)
  }

  it should "provide correct pagination for 13 results on page limit 10" in {
    val result = mockedService(10).getMatches("der", Language.LATIN, 1, 10)
    result.totalHits === 13
    result.hits.size() === 10
    (result.hasPreviousPage, result.hasNextPage) === (false, true)
  }


}
