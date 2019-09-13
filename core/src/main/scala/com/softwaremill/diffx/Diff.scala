package com.softwaremill.diffx

trait Diff[T] { outer =>
  def apply(left: T, right: T): DiffResult = apply(left, right, Nil)
  def apply(left: T, right: T, toIgnore: List[FieldPath]): DiffResult

  private[diffx] def ignoreUnsafe(fields: String*): Diff[T] = new Diff[T] {
    override def apply(left: T, right: T, toIgnore: List[FieldPath]): DiffResult =
      outer.apply(left, right, toIgnore ++ List(fields.toList))
  }

  def contramap[R](f: R => T): Diff[R] = (left: R, right: R, toIgnore: List[FieldPath]) => {
    outer(f(left), f(right), toIgnore)
  }

  def ignore[U](path: T => U): Diff[T] = macro IgnoreMacro.ignoreMacro[T, U]
}

object Diff {
  def apply[T: Diff]: Diff[T] = implicitly[Diff[T]]

  def identical[T]: Diff[T] = (left: T, _: T, _: List[FieldPath]) => Identical(left)

  // Implicit instance of Diff[T] created from implicit Derived[Diff[T]]
  implicit def anyDiff[T](implicit dd: Derived[Diff[T]]): Diff[T] = dd.value

  // Implicit conversion
  implicit def unwrapDerivedDiff[T](dd: Derived[Diff[T]]): Diff[T] = dd.value
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
