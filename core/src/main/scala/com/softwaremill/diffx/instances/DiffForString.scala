package com.softwaremill.diffx.instances

import com.softwaremill.diffx._
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
    rows.map {
      case DiffRow.Insert(newLine)     => DiffResultMissing(newLine)
      case DiffRow.Delete(oldLine)     => DiffResultAdditional(oldLine)
      case DiffRow.Change(oldLine, "") => DiffResultAdditional(oldLine)
      case DiffRow.Change("", newLine) => DiffResultMissing(newLine)
      case DiffRow.Change(oldLine, newLine) =>
        val oldSplit = tokenize(oldLine)
        val newSplit = tokenize(newLine)
        val wordDiffs = generator.generateDiffRows(
          oldSplit,
          newSplit
        )
        val words = processWordDiffs(wordDiffs)
        DiffResultStringLine(words)
      case DiffRow.Equal(oldLine, _) => IdenticalValue(oldLine)
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
    words.map {
      case DiffRow.Insert(newLine)     => DiffResultMissingChunk(newLine)
      case DiffRow.Delete(oldLine)     => DiffResultAdditionalChunk(oldLine)
      case DiffRow.Change("", newLine) => DiffResultMissingChunk(newLine)
      case DiffRow.Change(oldLine, "") => DiffResultAdditionalChunk(oldLine)
      case DiffRow.Change(oldLine, newLine) =>
        val charDiff = generator.generateDiffRows(
          oldLine.toList.map(_.toString),
          newLine.toList.map(_.toString)
        )
        val similarity = charDiff.count {
          case DiffRow.Equal(_, _) => true
          case _                   => false
        }.toDouble / charDiff.size
        if (similarity < similarityThreshold) {
          DiffResultValue(oldLine, newLine)
        } else {
          DiffResultStringWord(processCharDiffs(charDiff))
        }
      case DiffRow.Equal(oldLine, _) => IdenticalValue(oldLine)
    }
  }

  private def processCharDiffs(chars: List[DiffRow[String]]): List[DiffResult] = {
    chars.map {
      case DiffRow.Insert(newLine)          => DiffResultMissingChunk(newLine)
      case DiffRow.Delete(oldLine)          => DiffResultAdditionalChunk(oldLine)
      case DiffRow.Change("", newLine)      => DiffResultMissingChunk(newLine)
      case DiffRow.Change(oldLine, "")      => DiffResultAdditionalChunk(oldLine)
      case DiffRow.Change(oldLine, newLine) => DiffResultChunk(oldLine, newLine)
      case DiffRow.Equal(oldLine, _)        => IdenticalValue(oldLine)
    }
  }

  private def splitIntoLines(string: String) = {
    string.replace("\r\n", "\n").split("\n").toList
  }
}
