package de.fusionfactory.index_vivus.services

import de.fusionfactory.index_vivus.models.IDictionaryEntry
import java.util.{List => JList}
import java.lang.Math._

/**
 * offset and page counts form 1
 * 
 * Created by Markus Ackermann.
 * No rights reserved.
 */
case class FulltextResultPage(totalHits: Int, limit: Int, page: Int, hits: JList[_ <: IDictionaryEntry]) {

  protected def offset(page: Int) = ((page - 1) * limit) + 1
  
  def offset: Int = offset(page)
  
  def hasPreviousPage = page > 1
  
  def hasNextPage = offset(page + 1) <= totalHits

  def previousPageOffset = page - 1
  
  def nextPageOffset = page + 1
  
  def statString = s"Seite $page mit Treffern $offset...${min(nextPageOffset -1, totalHits)} ($totalHits Treffer insgesamt)}"
}
