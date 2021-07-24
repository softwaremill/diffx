package com.softwaremill.diffx.instances

import com.softwaremill.diffx._
import com.softwaremill.diffx.instances.string.DiffRow.Tag
import com.softwaremill.diffx.instances.string.{DiffRow, DiffRowGenerator}

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

  private def processLineDiffs(rows: List[DiffRow]) = {
    rows.map { row =>
      row.tag match {
        case Tag.INSERT => DiffResultMissing(row.newLine)
        case Tag.DELETE => DiffResultAdditional(row.oldLine)
        case Tag.CHANGE =>
          if (row.newLine.isEmpty) {
            DiffResultAdditional(row.oldLine)
          } else if (row.oldLine.isEmpty) {
            DiffResultMissing(row.newLine)
          } else {
            val oldSplit = tokenize(row.oldLine)
            val newSplit = tokenize(row.newLine)
            val wordDiffs = generator.generateDiffRows(
              oldSplit,
              newSplit
            )
            val words = processWordDiffs(wordDiffs)
            DiffResultStringLine(words)
          }
        case Tag.EQUAL =>
          IdenticalValue(row.newLine)
      }
    }
  }

  private def tokenize(line: String): List[String] = {
    line
      .foldLeft(List.empty[List[Char]]) { (acc, item) =>
        acc.lastOption match {
          case Some(word) =>
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

  private def processWordDiffs(words: List[DiffRow]): List[DiffResult] = {
    words.map { wordDiff =>
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
              wordDiff.oldLine.toList.map(_.toString),
              wordDiff.newLine.toList.map(_.toString)
            )
            val similarity = charDiff.count(_.tag == Tag.EQUAL).toDouble / charDiff.size
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

  private def processCharDiffs(chars: List[DiffRow]): List[DiffResult] = {
    chars.map { charDiff =>
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
    string.replace("\r\n", "\n").split("\n").toList
  }
}
