package com.softwaremill.diffx.instances

import com.softwaremill.diffx._
import com.softwaremill.diffx.instances.string.DiffRow.Tag
import com.softwaremill.diffx.instances.string.{DiffRow, DiffRowGenerator}

import java.util
import scala.collection.JavaConverters._

private[diffx] class DiffForString extends Diff[String] {
  private val generator = DiffRowGenerator.create
    .showInlineDiffs(true)
    .mergeOriginalRevised(false)
    .inlineDiffByWord(false)
    .build

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
      row.tag match {
        case Tag.INSERT => List(DiffResultMissing(row.newLine))
        case Tag.DELETE => List(DiffResultAdditional(row.oldLine))
        case Tag.CHANGE =>
          if (row.newLine.isEmpty) {
            List(DiffResultAdditional(row.oldLine))
          } else if (row.oldLine.isEmpty) {
            List(DiffResultMissing(row.newLine))
          } else {
            val lineDiffs = generator.generateDiffRows(splitIntoLines(row.oldLine), splitIntoLines(row.newLine))
            val b = processLineDiffs(lineDiffs)
            List(DiffResultStringLine(b))
          }
        case Tag.EQUAL => List(IdenticalValue(row.newLine))
      }
    }
  }

  private def processLineDiffs(rows: util.List[DiffRow]): List[DiffResult] = {
    rows.asScala.toList.flatMap { row =>
      row.tag match {
        case Tag.INSERT => List(DiffResultMissing(row.newLine))
        case Tag.DELETE => List(DiffResultAdditional(row.oldLine))
        case Tag.CHANGE =>
          if (row.newLine.isEmpty) {
            List(DiffResultAdditional(row.oldLine))
          } else if (row.oldLine.isEmpty) {
            List(DiffResultMissing(row.newLine))
          } else {
            val rows2 = generator.generateDiffRows(
              row.oldLine.split(' ').toList.asJava,
              row.newLine.split(' ').toList.asJava
            )
            processWordDiffs(rows2)
          }
        case Tag.EQUAL => List(IdenticalValue(row.newLine))
      }
    }
  }

  private def processWordDiffs(rows: util.List[DiffRow]): List[DiffResult] = {
    rows.asScala.toList.map { row =>
      row.tag match {
        case Tag.INSERT => DiffResultMissing(row.newLine)
        case Tag.DELETE => DiffResultAdditional(row.oldLine)
        case Tag.CHANGE =>
          if (row.newLine.isEmpty) {
            DiffResultAdditional(row.oldLine)
          } else if (row.oldLine.isEmpty) {
            DiffResultMissing(row.newLine)
          } else {
            DiffResultValue(row.oldLine, row.newLine)
          }
        case Tag.EQUAL => IdenticalValue(row.newLine)
      }
    }
  }

  private def splitIntoLines(string: String) = {
    string.trim().replace("\r\n", "\n").split("\n").toIndexedSeq.asJava
  }
}
