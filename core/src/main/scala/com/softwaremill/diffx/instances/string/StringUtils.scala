package com.softwaremill.diffx.instances.string

object StringUtils {
  def wrapText(line: String, columnWidth: Int): String = {
    if (columnWidth < 0) throw new IllegalArgumentException("columnWidth may not be less 0")
    if (columnWidth == 0) return line
    val length = line.length
    val delimiter = "<br/>".length
    var widthIndex = columnWidth
    val b = new StringBuilder(line)
    var count = 0
    while ({
      length > widthIndex
    }) {
      var breakPoint = widthIndex + delimiter * count
      if (Character.isHighSurrogate(b.charAt(breakPoint - 1)) && Character.isLowSurrogate(b.charAt(breakPoint))) { // Shift a breakpoint that would split a supplemental code-point.
        breakPoint += 1
        if (breakPoint == b.length) { // Break before instead of after if this is the last code-point.
          breakPoint -= 2
        }
      }
      b.insert(breakPoint, "<br/>")
      widthIndex += columnWidth

      count += 1
    }
    b.toString
  }
}
