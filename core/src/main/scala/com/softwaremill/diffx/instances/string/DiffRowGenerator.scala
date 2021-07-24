package com.softwaremill.diffx.instances.string

/*
 * Copyright 2009-2017 java-diff-utils.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util
import java.util._
import scala.collection.JavaConverters._

object DiffRowGenerator {

  def create: DiffRowGenerator = new DiffRowGenerator
}

final class DiffRowGenerator {

  /** Get the DiffRows describing the difference between original and revised
    * texts using the given patch. Useful for displaying side-by-side diff.
    *
    * @param original the original text
    * @param revised  the revised text
    * @return the DiffRows between original and revised texts
    */
  def generateDiffRows(original: util.List[String], revised: util.List[String]): util.List[DiffRow] = {
    val patch = DiffUtils.diff(original, revised)
    generateDiffRows(original, patch)
  }

  /** Generates the DiffRows describing the difference between original and
    * revised texts using the given patch. Useful for displaying side-by-side
    * diff.
    *
    * @param original the original text
    * @param patch    the given patch
    * @return the DiffRows between original and revised texts
    */
  def generateDiffRows(original: util.List[String], patch: Patch[String]): util.List[DiffRow] = {
    val diffRows = new util.ArrayList[DiffRow]
    var endPos = 0
    val deltaList = patch.getDeltas
    for (originalDelta <- deltaList.asScala) {
      for (delta <- decompressDeltas(originalDelta).asScala) {
        endPos = transformDeltaIntoDiffRow(original, endPos, diffRows, delta)
      }
    }
    // Copy the final matching chunk if any.
    for (line <- original.subList(endPos, original.size).asScala) {
      diffRows.add(buildDiffRow(DiffRow.Tag.EQUAL, line, line))
    }
    diffRows
  }

  /** Transforms one patch delta into a DiffRow object.
    */
  private def transformDeltaIntoDiffRow(
      original: util.List[String],
      endPos: Int,
      diffRows: util.List[DiffRow],
      delta: Delta[String]
  ) = {
    val orig = delta.getSource
    val rev = delta.getTarget
    for (line <- original.subList(endPos, orig.position).asScala) {
      diffRows.add(buildDiffRow(DiffRow.Tag.EQUAL, line, line))
    }
    delta.getType match {
      case Delta.TYPE.INSERT =>
        for (line <- rev.lines) {
          diffRows.add(buildDiffRow(DiffRow.Tag.INSERT, "", line))
        }

      case Delta.TYPE.DELETE =>
        for (line <- orig.lines) {
          diffRows.add(buildDiffRow(DiffRow.Tag.DELETE, line, ""))
        }

      case _ =>
        for (j <- 0 until Math.max(orig.size, rev.size)) {
          diffRows.add(
            buildDiffRow(
              DiffRow.Tag.CHANGE,
              if (orig.lines.size > j) orig.lines(j)
              else "",
              if (rev.lines.size > j) rev.lines(j)
              else ""
            )
          )
        }
    }
    orig.last + 1
  }

  /** Decompresses ChangeDeltas with different source and target size to a
    * ChangeDelta with same size and a following InsertDelta or DeleteDelta.
    * With this problems of building DiffRows getting smaller.
    *
    * @param deltaList
    */
  private def decompressDeltas(delta: Delta[String]): util.List[Delta[String]] = {
    if ((delta.getType == Delta.TYPE.CHANGE) && delta.getSource.size != delta.getTarget.size) {
      val deltas = new util.ArrayList[Delta[String]]
      val minSize = Math.min(delta.getSource.size, delta.getTarget.size)
      val orig = delta.getSource
      val rev = delta.getTarget
      deltas.add(
        new ChangeDelta[String](
          new Chunk[String](orig.position, orig.lines.slice(0, minSize)),
          new Chunk[String](rev.position, rev.lines.slice(0, minSize))
        )
      )
      if (orig.lines.size < rev.lines.size)
        deltas.add(
          new InsertDelta[String](
            new Chunk[String](orig.position + minSize, scala.List.empty),
            new Chunk[String](rev.position + minSize, rev.lines.slice(minSize, rev.lines.size))
          )
        )
      else
        deltas.add(
          new DeleteDelta[String](
            new Chunk[String](orig.position + minSize, orig.lines.slice(minSize, orig.lines.size)),
            new Chunk[String](rev.position + minSize, scala.List.empty)
          )
        )
      return deltas
    }
    Collections.singletonList(delta)
  }

  private def buildDiffRow(`type`: DiffRow.Tag, orgline: String, newline: String) =
    new DiffRow(`type`, orgline, newline)
}
