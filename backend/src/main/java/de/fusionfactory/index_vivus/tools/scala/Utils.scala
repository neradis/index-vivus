package de.fusionfactory.index_vivus.tools.scala

import com.google.common.base.Optional

/**
 * Created by Markus Ackermann.
 * No rights reserved. 
 */
object Utils {
  private val OPTIONAL_ABSENT = Optional.absent()
  private val OPTIONAL_ABSENT_INT : Optional[_ <: Integer] = Optional.absent()

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
      case Some(i) => Optional.of(i)
      case None => Optional.absent()
    }

    implicit def IntegerOptional2intOption(opt: Optional[Integer]): Option[Int] = opt match {
      case OPTIONAL_ABSENT_INT => None
      case opt => Some(opt.get())
    }

  }
}
