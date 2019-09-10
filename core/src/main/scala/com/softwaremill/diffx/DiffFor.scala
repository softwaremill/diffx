package com.softwaremill.diffx

trait DiffFor[T] { outer =>
  protected val ignored: List[List[String]] = List.empty

  def apply(left: T, right: T): DiffResult = apply(left, right, Nil)
  def apply(left: T, right: T, toIgnore: List[List[String]]): DiffResult

  private[diffx] def ignoreUnsafe(fields: String*): DiffFor[T] = new DiffFor[T] {
    override def apply(left: T, right: T, toIgnore: List[List[String]]): DiffResult =
      outer.apply(left, right, toIgnore ++ ignored)
    override val ignored: List[List[String]] = outer.ignored ++ List(fields.toList)
  }

  def contramap[R](f: R => T): DiffFor[R] = (left: R, right: R, toIgnore: List[List[String]]) => {
    outer(f(left), f(right), toIgnore ++ ignored)
  }
}

object DiffFor {
  def apply[T: DiffFor]: DiffFor[T] = implicitly[DiffFor[T]]

  def apply[T, R: DiffFor](converter: T => R): DiffFor[T] = DiffFor[R].contramap(converter)

  def identical[T]: DiffFor[T] = (left: T, _: T, _: List[List[String]]) => Identical(left)

  implicit def anyDiff[T](implicit dd: Derived[DiffFor[T]]): DiffFor[T] = dd.value
}

trait DiffResult {
  def isIdentical: Boolean
  def show: String = showIndented(5)

  private[diffx] def showIndented(indent: Int): String
}

case class DiffResultObject(name: String, fields: Map[String, DiffResult]) extends DiffResultDifferent {
  override private[diffx] def showIndented(indent: Int): String = {
    val showFields = fields.map(f => s"${i(indent)}${f._1}: ${f._2.showIndented(indent + 5)}")
    s"""$name(
       |${showFields.mkString("\n")})""".stripMargin
  }
}

case class DiffResultSet(diffs: List[DiffResult]) extends DiffResultDifferent {
  override private[diffx] def showIndented(indent: Int): String = {
    val showFields = diffs.map(f => s"${i(indent)}$f")
    s"""Set(
       |${showFields.mkString("\n")})""".stripMargin
  }
}

trait DiffResultDifferent extends DiffResult {
  override def isIdentical: Boolean = false
  protected def i(indent: Int) = " " * indent
}

case class DiffResultValue[T](left: T, right: T) extends DiffResultDifferent {
  override def showIndented(indent: Int): String = showChange(left.toString, right.toString)
}

case class Identical[T](value: T) extends DiffResult {
  override def isIdentical: Boolean = true
  override def showIndented(indent: Int): String = value.toString
}

case class DiffResultMissing[T](value: T) extends DiffResultDifferent {
  override def showIndented(indent: Int): String = {
    red(value.toString)
  }
}
case class DiffResultAdditional[T](value: T) extends DiffResultDifferent {
  override def showIndented(indent: Int): String = {
    green(value.toString)
  }
}

case class Derived[T](value: T)

object Derived {
  def apply[T: Derived]: Derived[T] = implicitly[Derived[T]]
}
