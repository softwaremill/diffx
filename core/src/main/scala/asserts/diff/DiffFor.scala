package asserts.diff

trait DiffFor[T] {

  def diff(left: T, right: T): DiffResult
}

object DiffFor {
  def apply[T, R: DiffFor](converter: T => R): DiffFor[T] =
    (left: T, right: T) => implicitly[DiffFor[R]].diff(converter(left), converter(right))
}

trait DiffResult {
  def show(indent: Int): String
}

case class DiffResultObject(name: String, fields: Map[String, DiffResult]) extends DiffResult {
  override def show(indent: Int): String =
    s"""$name(
       |${fields.map(f => s"${i(indent)}${f._1}: ${f._2.show(indent + 5)}").mkString("\n")})""".stripMargin

  private def i(indent: Int) = {
    List.fill(indent)(" ").mkString
  }
}

case class DiffResultValue[T](left: T, right: T) extends DiffResult {
  override def show(indent: Int): String = s"$left -> $right"
}

case class Identical2[T](value: T) extends DiffResult {
  override def show(indent: Int): String = value.toString
}

class IndentStringContext(sc: StringContext) {
  def e(args: Any*): String = {
    val sb = new StringBuilder()
    for ((s, a) <- sc.parts zip args) {
      sb append s

      Console.println(getindent(s).length)
      val ind = getindent(s)
      if (ind.size > 0) {
        sb append a.toString().replaceAll("\n", "\n" + ind)
      } else {
        sb append a.toString()
      }
    }
    if (sc.parts.size > args.size)
      sb append sc.parts.last

    sb.toString()
  }

  // get white indent after the last new line, if any
  def getindent(str: String): String = {
    val lastnl = str.lastIndexOf("\n")
    if (lastnl == -1) ""
    else {
      val ind = str.substring(lastnl + 1)
      if (ind.trim.isEmpty) ind // ind is all whitespace. Use this
      else ""
    }
  }
}

object Indenter {
  // top level implicit defs allowed only in 2.10 and above
  implicit def toISC(sc: StringContext): IndentStringContext = new IndentStringContext(sc)
}
