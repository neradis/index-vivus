package de.fusionfactory.index_vivus.services.scalaimpl

import de.fusionfactory.index_vivus.services.{FulltextResultPage, Language, IFullTextSearchService}
import de.fusionfactory.index_vivus.models.IDictionaryEntry
import java.util.{List => JList}
import de.fusionfactory.index_vivus.indexer.Indexer

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
object FullTextSearchService {
  lazy val indexerCache = new Indexer
}

class FullTextSearchService extends FullTextSearchServiceImpl with IndexSearchProvider {

  def indexer = FullTextSearchService.indexerCache
}


trait FullTextSearchServiceImpl extends IFullTextSearchService { this: IndexSearchProvider =>

  def getMatches(query: String, language: Language = Language.ALL, page: Int, limit: Int): FulltextResultPage = {

      require(page >= 1, "requested page must be => 1, since page counts start form 1")

      val listWithTotalCount = indexer.getSearchResults(query, language, limit, (page - 1) * limit)
      val total = listWithTotalCount.total

      def maxPageWhenResults = total / limit + (if (total % limit > 0) 1 else 0)
      require(page <= maxPageWhenResults || total == 0,
        s"not enough search results for requested page (max=$maxPageWhenResults; requested: $page)")

      FulltextResultPage(total, limit, page, listWithTotalCount.list)
    }

  def getMatches(query: String, page: Int, limit: Int): FulltextResultPage = getMatches(query, Language.ALL, page, limit)
}

trait IndexSearch {

  def getSearchResults(query: String, language: Language, limit: Int, offset: Int): DictionaryEntryListWithTotalCount
}

trait IndexSearchProvider {
  
  def indexer: IndexSearch
}


trait DictionaryEntryListWithTotalCount {

  def list: JList[_ <: IDictionaryEntry]

  def total: Int
}

case class DictionaryEntryListWithTotalCountImpl(list: JList[_ <: IDictionaryEntry], total: Int)
  extends DictionaryEntryListWithTotalCount
