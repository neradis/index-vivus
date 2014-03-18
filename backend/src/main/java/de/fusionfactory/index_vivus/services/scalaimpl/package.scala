package de.fusionfactory.index_vivus.services

import de.fusionfactory.index_vivus.models.WordType

/**
 * Created by Markus Ackermann.
 * No rights reserved.
 */
package object scalaimpl {

  lazy val posIdxRange = 0 until WordType.values.length
  lazy val langIdxRange = 0 until Language.values.length

  implicit def lang2Byte(wt: Language): Byte = Language.values.indexOf(wt).toByte

  implicit def byte2Lang(idx: Byte): Language = int2Lang(idx)

  implicit def int2Lang(idx: Int): Language = idx match {
    case i: Int if langIdxRange contains i => Language.values.apply(i)
    case _ => throw new IllegalStateException(s"index $idx out of bounds for ${classOf[Language].getSimpleName}")
  }

  implicit def pos2Byte(wt: WordType): Option[Byte] = wt match {
    case WordType.UNKNOWN => None
    case wt : WordType => Some(WordType.values.indexOf(wt).toByte)
  }

  implicit def byte2Pos(idx: Option[Byte]): WordType = idx match {
    case Some(b: Byte) if posIdxRange contains b => WordType.values.apply(b)
    case None => WordType.UNKNOWN
    case _ => throw new IllegalStateException(s"index $idx out of bounds for ${classOf[WordType].getSimpleName}")
  }
}
