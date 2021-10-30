package com.softwaremill.diffx.instances

import com.softwaremill.diffx._
import com.softwaremill.diffx.instances.string.DiffRow.Tag
import com.softwaremill.diffx.instances.string.{DiffRow, DiffRowGenerator}

class DiffForString(similarityThreshold: Double = 0.5) extends Diff[String] {
  private val generator = new DiffRowGenerator

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

  private def processLineDiffs(rows: List[DiffRow[String]]) = {
    rows.map { row =>
      row.tag match {
        case Tag.INSERT => DiffResultMissing(row.newLine.get)
        case Tag.DELETE => DiffResultAdditional(row.oldLine.get)
        case Tag.CHANGE =>
          if (row.newLine.get.isEmpty) {
            DiffResultAdditional(row.oldLine.get)
          } else if (row.oldLine.get.isEmpty) {
            DiffResultMissing(row.newLine.get)
          } else {
            val oldSplit = row.oldLine.map(tokenize).getOrElse(List.empty)
            val newSplit = row.newLine.map(tokenize).getOrElse(List.empty)
            val wordDiffs = generator.generateDiffRows(
              oldSplit,
              newSplit
            )
            val words = processWordDiffs(wordDiffs)
            DiffResultStringLine(words)
          }
        case Tag.EQUAL =>
          IdenticalValue(row.oldLine.get)
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

  private def processWordDiffs(words: List[DiffRow[String]]): List[DiffResult] = {
    words.map { wordDiff =>
      wordDiff.tag match {
        case Tag.INSERT => DiffResultMissingChunk(wordDiff.newLine.get) // TODO convert Row to ADT
        case Tag.DELETE => DiffResultAdditionalChunk(wordDiff.oldLine.get)
        case Tag.CHANGE =>
          if (wordDiff.newLine.get.isEmpty) {
            DiffResultAdditionalChunk(wordDiff.oldLine.get)
          } else if (wordDiff.oldLine.get.isEmpty) {
            DiffResultMissingChunk(wordDiff.newLine.get)
          } else {
            val charDiff = generator.generateDiffRows(
              wordDiff.oldLine.get.toList.map(_.toString),
              wordDiff.newLine.get.toList.map(_.toString)
            )
            val similarity = charDiff.count(_.tag == Tag.EQUAL).toDouble / charDiff.size
            if (similarity < similarityThreshold) {
              DiffResultValue(wordDiff.oldLine.get, wordDiff.newLine.get)
            } else {
              DiffResultStringWord(processCharDiffs(charDiff))
            }
          }
        case Tag.EQUAL => IdenticalValue(wordDiff.oldLine.get)
      }
    }
  }

  private def processCharDiffs(chars: List[DiffRow[String]]): List[DiffResult] = {
    chars.map { charDiff =>
      charDiff.tag match {
        case Tag.INSERT => DiffResultMissingChunk(charDiff.newLine.get)
        case Tag.DELETE => DiffResultAdditionalChunk(charDiff.oldLine.get)
        case Tag.CHANGE =>
          if (charDiff.newLine.get.isEmpty) {
            DiffResultAdditionalChunk(charDiff.oldLine.get)
          } else if (charDiff.oldLine.get.isEmpty) {
            DiffResultMissingChunk(charDiff.newLine.get)
          } else {
            DiffResultChunk(charDiff.oldLine.get, charDiff.newLine.get)
          }
        case Tag.EQUAL => IdenticalValue(charDiff.oldLine.get)
      }
    }
  }

  private def splitIntoLines(string: String) = {
    string.replace("\r\n", "\n").split("\n").toList
  }
}
