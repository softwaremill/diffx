package com.softwaremill.diffx.instances

import com.softwaremill.diffx._
import com.softwaremill.diffx.instances.string.DiffRow.Tag
import com.softwaremill.diffx.instances.string.{DiffRow, DiffRowGenerator}

import java.util
import scala.::
import scala.collection.JavaConverters._

class DiffForString(similarityThreshold: Double = 0.5) extends Diff[String] {
  private val generator = DiffRowGenerator.create

  override def apply(left: String, right: String, context: DiffContext): DiffResult =
    nullGuard(left, right) { (left, right) =>
      val rows = generator.generateDiffRows(splitIntoLines(left), splitIntoLines(right))
      val lineResults = processLineDiffs(rows)
      if (lineResults.forall(_.isIdentical)) {
        IdenticalValue(left)
      } else {
        DiffResultString(lineResults)
      }
    }

  private def processLineDiffs(rows: util.List[DiffRow]) = {
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

            val oldSplit = splitThem(row.oldLine)
            val newSplit = splitThem(row.newLine)
            val wordDiffs = generator.generateDiffRows(
              oldSplit.asJava,
              newSplit.asJava
            )
            val words = processWordDiffs(wordDiffs)
            DiffResultStringLine(words)
          }
        case Tag.EQUAL =>
          IdenticalValue(row.newLine)
      }
    }
  }

  private def splitThem(line: String): List[String] = {
    line
      .foldLeft(List.empty[List[Char]]) { (acc, item) =>
        acc.lastOption match {
          case Some(word) =>
//            if (word.lastOption.contains(' ')) {
//              acc :+ List(item)
//            } else {
//              acc.dropRight(1) :+ (word ++ List(item))
//            }
            if (item == ' ') {
              acc ++ List(List(item))
            } else {
              if (word.lastOption.contains(' ')) {
                acc :+ List(item)
              } else {
                acc.dropRight(1) :+ (word ++ List(item))
              }
            }
          case None => acc :+ List(item)
        }
      }
      .map(_.mkString)
  }

  private def processWordDiffs(words: util.List[DiffRow]): List[DiffResult] = {
    words.asScala.toList.map { wordDiff =>
      wordDiff.tag match {
        case Tag.INSERT => DiffResultMissingChunk(wordDiff.newLine)
        case Tag.DELETE => DiffResultAdditionalChunk(wordDiff.oldLine)
        case Tag.CHANGE =>
          if (wordDiff.newLine.isEmpty) {
            DiffResultAdditionalChunk(wordDiff.oldLine)
          } else if (wordDiff.oldLine.isEmpty) {
            DiffResultMissingChunk(wordDiff.newLine)
          } else {
            val charDiff = generator.generateDiffRows(
              wordDiff.oldLine.toList.map(_.toString).asJava,
              wordDiff.newLine.toList.map(_.toString).asJava
            )
            val similarity = charDiff.asScala.toList.count(_.tag == Tag.EQUAL).toDouble / charDiff.size()
            if (similarity < similarityThreshold) {
              DiffResultValue(wordDiff.oldLine, wordDiff.newLine)
            } else {
              DiffResultStringWord(processCharDiffs(charDiff))
            }
          }
        case Tag.EQUAL => IdenticalValue(wordDiff.newLine)
      }
    }
  }

  private def processCharDiffs(chars: util.List[DiffRow]): List[DiffResult] = {
    chars.asScala.toList.map { charDiff =>
      charDiff.tag match {
        case Tag.INSERT => DiffResultMissingChunk(charDiff.newLine)
        case Tag.DELETE => DiffResultAdditionalChunk(charDiff.oldLine)
        case Tag.CHANGE =>
          if (charDiff.newLine.isEmpty) {
            DiffResultAdditionalChunk(charDiff.oldLine)
          } else if (charDiff.oldLine.isEmpty) {
            DiffResultMissingChunk(charDiff.newLine)
          } else {
            DiffResultChunk(charDiff.oldLine, charDiff.newLine)
          }
        case Tag.EQUAL => IdenticalValue(charDiff.newLine)
      }
    }
  }

  private def splitIntoLines(string: String) = {
    string.replace("\r\n", "\n").split("\n").toIndexedSeq.asJava
  }
}
