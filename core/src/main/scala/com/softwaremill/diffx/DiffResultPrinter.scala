package com.softwaremill.diffx

object DiffResultPrinter {
  private[diffx] final val IndentLevel = 5

  def showIndented(diffResult: DiffResult, indent: Int)(implicit sc: ShowConfig): String = {
    diffResult match {
      case dr: DiffResultObject        => showDiffResultObject(dr, indent)
      case dr: DiffResultIterable      => showDiffResultIterable(dr, indent)
      case dr: DiffResultMap           => showDiffResultMap(dr, indent)
      case dr: DiffResultSet           => showDiffResultSet(dr, indent)
      case dr: DiffResultString        => s"${dr.diffs.map(ds => showIndented(ds, indent)).mkString("\n")}"
      case dr: DiffResultStringLine    => mergeChunks(dr.diffs).map(ds => showIndented(ds, indent)).mkString
      case dr: DiffResultStringWord    => mergeChunks(dr.diffs).map(ds => showIndented(ds, indent)).mkString
      case dr: DiffResultChunk         => arrowColor("[") + showChange(s"${dr.left}", s"${dr.right}") + arrowColor("]")
      case dr: DiffResultValue[_]      => showChange(s"${dr.left}", s"${dr.right}")
      case dr: IdenticalValue[_]       => defaultColor(s"${dr.value}")
      case dr: DiffResultMissing[_]    => leftColor(s"${dr.value}")
      case dr: DiffResultMissingChunk  => leftColor(s"[${dr.value}]")
      case dr: DiffResultAdditional[_] => rightColor(s"${dr.value}")
      case dr: DiffResultAdditionalChunk => rightColor(s"[${dr.value}]")
    }
  }

  private def showDiffResultObject(resultObject: DiffResultObject, indent: Int)(implicit sc: ShowConfig): String = {
    def renderValue(value: DiffResult) = s"${showIndented(value, indent + IndentLevel)}"
    def renderField(field: String) = s"${i(indent)}${defaultColor(s"$field: ")}"

    val showFields = resultObject.fields
      .map { case (field, value) =>
        renderField(field) + renderValue(value)
      }
    defaultColor(s"${resultObject.name}(") + s"\n${showFields.mkString(defaultColor(",") + "\n")}" + defaultColor(")")
  }

  private def showDiffResultIterable(resultObject: DiffResultIterable, indent: Int)(implicit sc: ShowConfig): String = {
    def renderValue(value: DiffResult) = s"${showIndented(value, indent + IndentLevel)}"
    def renderField(field: String) = s"${i(indent)}${defaultColor(s"$field: ")}"

    val showFields = resultObject.items
      .map { case (field, value) =>
        renderField(field) + renderValue(value)
      }
    defaultColor(s"${resultObject.typename}(") + s"\n${showFields.mkString(defaultColor(",") + "\n")}" + defaultColor(
      ")"
    )
  }

  private def showDiffResultMap(diffResultMap: DiffResultMap, indent: Int)(implicit
      c: ShowConfig
  ): String = {
    def renderValue(value: DiffResult) = showIndented(value, indent + IndentLevel)
    def renderKey(key: DiffResult) = s"${i(indent)}${defaultColor(s"${showIndented(key, indent + IndentLevel)}")}"

    val showFields = diffResultMap.entries
      .map { case (k, v) =>
        val key = renderKey(k)
        val separator = defaultColor(": ")
        val value = renderValue(v)
        key + separator + value
      }
    defaultColor(s"${diffResultMap.typename}(") + s"\n${showFields.mkString(defaultColor(",") + "\n")}" + defaultColor(
      ")"
    )
  }

  private def showDiffResultSet(diffResultSet: DiffResultSet, indent: Int)(implicit
      sc: ShowConfig
  ): String = {
    val showFields = diffResultSet.diffs
      .map(f => s"${i(indent)}${showIndented(f, indent + IndentLevel)}")
    showFields.mkString(defaultColor(s"${diffResultSet.typename}(\n"), ",\n", defaultColor(")"))
  }

  private def i(indent: Int): String = " " * indent

  private def mergeChunks(diffs: List[DiffResult]) = {
    diffs
      .foldLeft(List.empty[DiffResult]) { (acc, item) =>
        (acc.lastOption, item) match {
          case (Some(d: DiffResultMissingChunk), di: DiffResultMissingChunk) =>
            acc.dropRight(1) :+ d.copy(value = d.value + di.value)
          case (Some(d: DiffResultAdditionalChunk), di: DiffResultAdditionalChunk) =>
            acc.dropRight(1) :+ d.copy(value = d.value + di.value)
          case (Some(d: DiffResultChunk), di: DiffResultChunk) =>
            acc.dropRight(1) :+ d.copy(left = d.left + di.left, right = d.right + di.right)
          case _ => acc :+ item
        }
      }
  }

  private def leftColor(s: String)(implicit c: ShowConfig): String = c.left(s)
  private def rightColor(s: String)(implicit c: ShowConfig): String = c.right(s)
  private def defaultColor(s: String)(implicit c: ShowConfig): String = c.default(s)
  private def arrowColor(s: String)(implicit c: ShowConfig): String = c.arrow(s)
  private def showChange(l: String, r: String)(implicit c: ShowConfig): String =
    leftColor(l) + arrowColor(" -> ") + rightColor(r)
}
