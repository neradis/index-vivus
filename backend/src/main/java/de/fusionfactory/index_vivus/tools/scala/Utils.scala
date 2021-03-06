package de.fusionfactory.index_vivus.tools.scala

import com.google.common.base.Optional
import java.lang.{Boolean => JBoolean}
import de.fusionfactory.index_vivus.services.Language
import de.fusionfactory.index_vivus.services
import com.google.common.io.Resources
import java.io.File
import com.google.common.collect.ImmutableList


/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
object Utils {
  private val OPTIONAL_ABSENT = Optional.absent()
  private val OPTIONAL_ABSENT_INT : Optional[_ <: Integer] = Optional.absent()
  private val OPTIONAL_ABSENT_BOOLEAN : Optional[_ <: JBoolean] = Optional.absent()

  def matchingNone[T]: Option[T] = Option.empty[T]

  def lang2Byte(l: Language) = services.scalaimpl.lang2Byte(l)

  def fileForResouce(resourceName: String) = {
    new File(Resources.getResource(resourceName).toURI)
  }

  def moveMetaDataFilesToFront(fileArray: Array[File]): ImmutableList[File] = {
    def isMetaData(f: File) = f.getName.contains("000")
    val sortedFiles = fileArray.sortBy(_.getName)
    ImmutableList.copyOf(sortedFiles.filter(isMetaData) ++ sortedFiles.filterNot(isMetaData))
  }
  
  object OptionConversions {

    implicit def optional2Option[T](opt: Optional[T]): Option[T] = opt match {
      case OPTIONAL_ABSENT => None
      case opt => Some(opt.get())
    }

    implicit def option2Optional[T](opt: Option[T]): Optional[T] = opt match {
      case None => Optional.absent()
      case Some(x) => Optional.of(x)
    }

    implicit def intOption2IntegerOptional(opt: Option[Int]): Optional[Integer] = opt match {
      case Some(i: Int) => Optional.of(int2Integer(i).toInt)
      case None => Optional.absent()
    }

    implicit def integerOptional2intOption(opt: Optional[Integer]): Option[Int] = opt match {
      case OPTIONAL_ABSENT_INT => None
      case opt => Some(opt.get())
    }

    implicit def booleanOption2BooleanOptional(opt: Option[Boolean]): Optional[JBoolean] = opt match {
      case Some(b: Boolean) => Optional.of(b)
      case None => Optional.absent()
    }

    implicit def booleanOptional2BooleanOption(opt: Optional[JBoolean]): Option[Boolean] = opt match {
      case OPTIONAL_ABSENT_BOOLEAN => None
      case opt => Some(opt.get())
    }
  }
}
