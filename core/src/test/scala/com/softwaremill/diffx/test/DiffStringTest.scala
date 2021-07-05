package com.softwaremill.diffx.test

import com.github.difflib.DiffUtils
import com.github.difflib.patch.{AbstractDelta, ChangeDelta, Chunk, DeleteDelta, EqualDelta, InsertDelta, Patch}
import com.github.difflib.text.DiffRow.Tag
import com.github.difflib.text.{DiffRow, DiffRowGenerator2}
import com.softwaremill.diffx.instances.string.AADiff
import com.softwaremill.diffx.{
  ConsoleColorConfig,
  Diff,
  DiffContext,
  DiffResult,
  DiffResultAdditional,
  DiffResultMissing,
  DiffResultString,
  DiffResultValue,
  IdenticalValue
}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.util
import scala.collection.JavaConverters._

//TODO diffUtil whole text
//for each different line where left is not empty & right is not empty
//perform diffUtil => save as DiffResultLine(rs: List[DiffResult])
class DiffStringTest extends AnyFreeSpec with Matchers {
  "1" in {
    val right = "kamil\nqwe"
    val left = "kasper cos tam\nkamil\nqwe"
    display(left, right)
  }

  "2" in {
    val left = "kamil kamil"
    val right = "kamil kamil kamil"
    display(left, right)
  }

  "3" in {
    val left = "kamil kamil kamil"
    val right = "kamil kamil"
    display(left, right)
  }

  "4" in {
    val right =
      """text1
        |text2 text2 text4
        |text3
        |text4
        |""".stripMargin
    val left =
      """text1
        |text2 text3 text4
        |text3
        |text4
        |""".stripMargin
    display(right, left)
  }

  "5" in {
    val right =
      """text1
        |text2text2text4aaaaaaaaaaaaaaaaaaaaa
        |text3
        |text4
        |""".stripMargin
    val left =
      """text1
        |text2text3text4aaaaaaaaaaaaaaaaaaaaa
        |text3
        |text4
        |""".stripMargin
    display(right, left)
  }

  private def display(right: String, left: String) = {
    println("====ex====")
    println(new AADiff(left, right).unifiedDiff)
//    println("====regular====")
//    println(Diff.diffForString.apply(left, right).show())
    println("====better====")
    println(new DiffBetterString().apply(left, right).show())
//    println("====mystr====")
//    println(new MyStrDiff().apply(left, right))
  }
}

class DiffBetterString extends Diff[String] {
  val strike = "\u0336"
  val generator = DiffRowGenerator2
    .create()
    .showInlineDiffs(false)
    .mergeOriginalRevised(false)
    .inlineDiffByWord(false)
    .oldTag(_ => "") //introduce markdown style for strikethrough
    .newTag(_ => "") //introduce markdown style for bold
    .build();
  override def apply(left: String, right: String, context: DiffContext): DiffResult = {

    val rows = generator.generateDiffRows(splitIntoLines(left), splitIntoLines(right))
    val a = process(rows)
    a
  }

  private def process(rows: util.List[DiffRow]): DiffResult = {
    DiffResultString(join(rows.asScala.toList.flatMap { row =>
      row.getTag match {
        case Tag.INSERT => List(DiffResultMissing(row.getNewLine))
        case Tag.DELETE => List(DiffResultAdditional(row.getOldLine))
        case Tag.CHANGE =>
          if (row.getNewLine.isEmpty) {
            List(DiffResultAdditional(row.getOldLine))
          } else if (row.getOldLine.isEmpty) {
            List(DiffResultMissing(row.getNewLine))
          } else {
            val rows2 = generator.generateDiffRows(splitIntoLines(row.getOldLine), splitIntoLines(row.getNewLine))
            val b = join(processWords(rows2))(IdenticalValue(" "))
            List(DiffResultString(b))
          }
        case Tag.EQUAL => List(IdenticalValue(row.getNewLine))
      }
    })(IdenticalValue("\n")))
  }

  private def process2(rows: util.List[util.List[AbstractDelta[String]]]): DiffResult = {
    DiffResultString(join(rows.asScala.toList.flatMap { lineDiff =>
      lineDiff.asScala.toList.map { wordDiff =>
        wordDiff match {
          case delta: ChangeDelta[_] => DiffResultValue(delta.getSource.toString, delta.getTarget.toString)
          case delta: DeleteDelta[_] => DiffResultMissing(delta.getSource.toString)
          case delta: EqualDelta[_]  => IdenticalValue(delta.getSource.toString)
          case delta: InsertDelta[_] => DiffResultAdditional(delta.getSource.toString)
        }
      }
    })(IdenticalValue("\n")))
  }

  private def join(results: List[DiffResult])(sep: DiffResult): List[DiffResult] = {
    results.zipWithIndex.flatMap { case (e, i) =>
      if (i == 0) {
        List(e)
      } else {
        List(sep, e)
      }
    }
  }

  private def processWords(rows: util.List[DiffRow]): List[DiffResult] = {
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
            processLast(rows2)
          }
        case Tag.EQUAL => List(IdenticalValue(row.getNewLine))
      }
    }
  }

  private def processLast(rows: util.List[DiffRow]): List[DiffResult] = {
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
//class MyStrDiff() extends Diff[String] {
//  override def apply(left: String, right: String, context: DiffContext): DiffResult = {
//    val leftLines = splitIntoLines(left)
//    val rightLines = splitIntoLines(right)
//    val result = DiffUtils.diff(leftLines, rightLines, DiffRowGenerator.DEFAULT_EQUALIZER)
//    val dd = result.getDeltas.asScala
//      .map { d =>
//        println(
//          s"source: ${d.getSource.getPosition} ${d.getSource.size()}, target: ${d.getTarget.getPosition} ${d.getTarget.size()}"
//        )
//        d
//      }
//      .map {
//        case delta: ChangeDelta[_] => DiffResultValue(delta.getSource, delta.getTarget)
//        case delta: DeleteDelta[_] =>
//          DiffResultMissing(delta.getSource.getLines.asScala.mkString)
//        case delta: InsertDelta[_] => DiffResultAdditional(delta.getTarget.getLines.asScala.mkString)
//      }
//    result.getDeltas.asScala.foldLeft(leftLines.asScala.toList.map(IdenticalValue(_)))((acc, item) => acc.patch(item.getSource.getPosition,))
//    DiffResultString(dd.toList)
//  }
//
//  private def splitIntoLines(string: String) = {
//    string.trim().replace("\r\n", "\n").split("\n").toIndexedSeq.asJava
//  }
//}
