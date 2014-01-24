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
  lazy val indexer = new Indexer
}

class FullTextSearchService extends IFullTextSearchService{
    import FullTextSearchService._

    def getMatches(query: String, language: Language = Language.ALL, page: Int, limit: Int): FulltextResultPage = {


      val clist = indexer.getSearchResults(query, language, limit, (page -1) * limit )
      FulltextResultPage(clist.total, limit, page, clist.list)
    }

  def getMatches(query: String, page: Int, limit: Int): FulltextResultPage = getMatches(query, Language.ALL, page, limit)
}


case class DictionaryEntryListWithTotalCount(list: JList[_ <: IDictionaryEntry], total: Int)