package com.softwaremill.diffx.instances

import com.github.difflib.text.DiffRow.Tag
import com.github.difflib.text.{DiffRow, DiffRowGenerator}
import com.softwaremill.diffx._

import java.util
import scala.collection.JavaConverters._

private[diffx] class DiffForString extends Diff[String] {
  private val generator = DiffRowGenerator
    .create()
    .showInlineDiffs(false)
    .mergeOriginalRevised(false)
    .inlineDiffByWord(false)
    .oldTag(_ => "")
    .newTag(_ => "")
    .build()

  override def apply(left: String, right: String, context: DiffContext): DiffResult =
    nullGuard(left, right) { (left, right) =>
      val rows = generator.generateDiffRows(splitIntoLines(left), splitIntoLines(right))
      val lineResults = processTextDiffs(rows)
      if (lineResults.forall(_.isIdentical)) {
        IdenticalValue(left)
      } else {
        DiffResultString(lineResults)
      }
    }

  private def processTextDiffs(rows: util.List[DiffRow]) = {
    rows.asScala.toList.flatMap { row =>
      row.getTag match {
        case Tag.INSERT => List(DiffResultMissing(row.getNewLine))
        case Tag.DELETE => List(DiffResultAdditional(row.getOldLine))
        case Tag.CHANGE =>
          if (row.getNewLine.isEmpty) {
            List(DiffResultAdditional(row.getOldLine))
          } else if (row.getOldLine.isEmpty) {
            List(DiffResultMissing(row.getNewLine))
          } else {
            val lineDiffs = generator.generateDiffRows(splitIntoLines(row.getOldLine), splitIntoLines(row.getNewLine))
            val b = processLineDiffs(lineDiffs)
            List(DiffResultStringLine(b))
          }
        case Tag.EQUAL => List(IdenticalValue(row.getNewLine))
      }
    }
  }

  private def processLineDiffs(rows: util.List[DiffRow]): List[DiffResult] = {
    rows.asScala.toList.flatMap { row =>
      row.getTag match {
        case Tag.INSERT => List(DiffResultMissing(row.getNewLine))
        case Tag.DELETE => List(DiffResultAdditional(row.getOldLine))
        case Tag.CHANGE =>
          if (row.getNewLine.isEmpty) {
            List(DiffResultAdditional(row.getOldLine))
          } else if (row.getOldLine.isEmpty) {
            List(DiffResultMissing(row.getNewLine))
          } else {
            val rows2 = generator.generateDiffRows(
              row.getOldLine.split(' ').toList.asJava,
              row.getNewLine.split(' ').toList.asJava
            )
            processWordDiffs(rows2)
          }
        case Tag.EQUAL => List(IdenticalValue(row.getNewLine))
      }
    }
  }

  private def processWordDiffs(rows: util.List[DiffRow]): List[DiffResult] = {
    rows.asScala.toList.map { row =>
      row.getTag match {
        case Tag.INSERT => DiffResultMissing(row.getNewLine)
        case Tag.DELETE => DiffResultAdditional(row.getOldLine)
        case Tag.CHANGE =>
          if (row.getNewLine.isEmpty) {
            DiffResultAdditional(row.getOldLine)
          } else if (row.getOldLine.isEmpty) {
            DiffResultMissing(row.getNewLine)
          } else {
            DiffResultValue(row.getOldLine, row.getNewLine)
          }
        case Tag.EQUAL => IdenticalValue(row.getNewLine)
      }
    }
  }

  private def splitIntoLines(string: String) = {
    string.trim().replace("\r\n", "\n").split("\n").toIndexedSeq.asJava
  }
}
