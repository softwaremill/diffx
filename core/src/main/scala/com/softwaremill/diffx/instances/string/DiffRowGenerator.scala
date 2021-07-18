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

import java.util._
import java.util
import java.util.function.BiFunction
import java.util.function.BiPredicate
import java.util.function.Function
import java.util.regex.Pattern
import java.util.stream.Collectors.toList

/** This class for generating DiffRows for side-by-sidy view. You can customize
  * the way of generating. For example, show inline diffs on not, ignoring white
  * spaces or/and blank lines and so on. All parameters for generating are
  * optional. If you do not specify them, the class will use the default values.
  *
  * These values are: showInlineDiffs = false; ignoreWhiteSpaces = true;
  * ignoreBlankLines = true; ...
  *
  * For instantiating the DiffRowGenerator you should use the its builder. Like
  * in example  <code>
  * DiffRowGenerator generator = new DiffRowGenerator.Builder().showInlineDiffs(true).
  * ignoreWhiteSpaces(true).columnWidth(100).build();
  * </code>
  */
object DiffRowGenerator {
  val DEFAULT_EQUALIZER: BiPredicate[String, String] = new BiPredicate[String, String] {
    override def test(t: String, u: String): Boolean = t == u
  }
  val IGNORE_WHITESPACE_EQUALIZER: BiPredicate[String, String] = (original: String, revised: String) =>
    adjustWhitespace(original) == adjustWhitespace(revised)
  val LINE_NORMALIZER_FOR_HTML: Function[String, String] = identity[String]

  /** Splitting lines by character to achieve char by char diff checking.
    */
  val SPLITTER_BY_CHARACTER: Function[String, util.List[String]] = (line: String) => {
    def foo(line: String) = {
      val list = new util.ArrayList[String](line.length)
      for (character <- line.toCharArray) {
        list.add(character.toString)
      }
      list
    }

    foo(line)
  }
  val SPLIT_BY_WORD_PATTERN: Pattern = Pattern.compile("\\s+|[,.\\[\\](){}/\\\\*+\\-#]")

  /** Splitting lines by word to achieve word by word diff checking.
    */
  val SPLITTER_BY_WORD: Function[String, util.List[String]] = (line: String) =>
    splitStringPreserveDelimiter(line, SPLIT_BY_WORD_PATTERN)
  val WHITESPACE_PATTERN: Pattern = Pattern.compile("\\s+")

  def create = new DiffRowGenerator.Builder

  private def adjustWhitespace(raw: String) = WHITESPACE_PATTERN.matcher(raw.trim).replaceAll(" ")

  protected def splitStringPreserveDelimiter(str: String, SPLIT_PATTERN: Pattern): util.List[String] = {
    val list = new util.ArrayList[String]
    if (str != null) {
      val matcher = SPLIT_PATTERN.matcher(str)
      var pos = 0
      while ({
        matcher.find
      }) {
        if (pos < matcher.start) list.add(str.substring(pos, matcher.start))
        list.add(matcher.group)
        pos = matcher.end
      }
      if (pos < str.length) list.add(str.substring(pos))
    }
    list
  }

  /** Wrap the elements in the sequence with the given tag
    *
    * @param startPosition the position from which tag should start. The
    *                      counting start from a zero.
    * @param endPosition   the position before which tag should should be closed.
    * @param tagGenerator  the tag generator
    */
  private def wrapInTag(
      sequence: util.List[String],
      startPosition: Int,
      endPosition: Int,
      tag: DiffRow.Tag,
      tagGenerator: BiFunction[DiffRow.Tag, Boolean, String],
      processDiffs: Function[String, String],
      replaceLinefeedWithSpace: Boolean
  ): Unit = {
    var endPos = endPosition
    var v0: Boolean = true
    var v1: Boolean = true
    var v2: Boolean = true
    while ({
      endPos >= startPosition && v0
    }) { //search position for end tag
      v1 = true
      while ({
        endPos > startPosition && v1
      }) {
        if (!("\n" == sequence.get(endPos - 1))) {
          v1 = false
        } else if (replaceLinefeedWithSpace) {
          sequence.set(endPos - 1, " ")
          v1 = false

        }
        endPos -= 1
      }
      if (endPos == startPosition) {
        v0 = false
      } else {
        sequence.add(endPos, tagGenerator.apply(tag, false))
        if (processDiffs != null) sequence.set(endPos - 1, processDiffs.apply(sequence.get(endPos - 1)))
        endPos -= 1
        v2 = true
        while ({
          endPos > startPosition && v2
        }) {
          if ("\n" == sequence.get(endPos - 1))
            if (replaceLinefeedWithSpace) sequence.set(endPos - 1, " ")
            else v2 = false
          if (v2) {
            if (processDiffs != null) sequence.set(endPos - 1, processDiffs.apply(sequence.get(endPos - 1)))
            endPos -= 1
          }
        }
        sequence.add(endPos, tagGenerator.apply(tag, true))
        endPos -= 1
      }
    }
  }

  /** This class used for building the DiffRowGenerator.
    *
    * @author dmitry
    */
  class Builder {
    var showInlineDiffs = false
    var ignoreWhiteSpaces = false
    var columnWidth = 0
    var mergeOriginalRevised = false
    var reportLinesUnchanged = false
    var inlineDiffSplitter = SPLITTER_BY_CHARACTER
    var lineNormalizer = LINE_NORMALIZER_FOR_HTML
    var processDiffs: Function[String, String] = null
    var equalizer: Function[String, String] = null
    var replaceOriginalLinefeedInChangesWithSpaces = false

    /** Show inline diffs in generating diff rows or not.
      *
      * @param val the value to set. Default: false.
      * @return builder with configured showInlineDiff parameter
      */
    def showInlineDiffs(`val`: Boolean): DiffRowGenerator.Builder = {
      showInlineDiffs = `val`
      this
    }

    /** Ignore white spaces in generating diff rows or not.
      *
      * @param val the value to set. Default: true.
      * @return builder with configured ignoreWhiteSpaces parameter
      */
    def ignoreWhiteSpaces(`val`: Boolean): DiffRowGenerator.Builder = {
      ignoreWhiteSpaces = `val`
      this
    }

    /** Give the originial old and new text lines to Diffrow without any
      * additional processing and without any tags to highlight the change.
      *
      * @param val the value to set. Default: false.
      * @return builder with configured reportLinesUnWrapped parameter
      */
    def reportLinesUnchanged(`val`: Boolean): DiffRowGenerator.Builder = {
      reportLinesUnchanged = `val`
      this
    }

    /** Processor for diffed text parts. Here e.g. whitecharacters could be
      * replaced by something visible.
      *
      * @param processDiffs
      * @return
      */
    def processDiffs(processDiffs: Function[String, String]): DiffRowGenerator.Builder = {
      this.processDiffs = processDiffs
      this
    }

    /** Set the column width of generated lines of original and revised
      * texts.
      *
      * @param width the width to set. Making it < 0 doesn't make any sense.
      *              Default 80. @return builder with config of column width
      */
    def columnWidth(width: Int): DiffRowGenerator.Builder = {
      if (width >= 0) columnWidth = width
      this
    }

    /** Build the DiffRowGenerator. If some parameters is not set, the
      * default values are used.
      *
      * @return the customized DiffRowGenerator
      */
    def build = new DiffRowGenerator(this)

    /** Merge the complete result within the original text. This makes sense
      * for one line display.
      *
      * @param mergeOriginalRevised
      * @return
      */
    def mergeOriginalRevised(mergeOriginalRevised: Boolean): DiffRowGenerator.Builder = {
      this.mergeOriginalRevised = mergeOriginalRevised
      this
    }

    /** Per default each character is separatly processed. This variant
      * introduces processing by word, which does not deliver in word
      * changes. Therefore the whole word will be tagged as changed:
      *
      * <pre>
      * false:    (aBa : aba) --  changed: a(B)a : a(b)a
      * true:     (aBa : aba) --  changed: (aBa) : (aba)
      * </pre>
      */
    def inlineDiffByWord(inlineDiffByWord: Boolean): DiffRowGenerator.Builder = {
      inlineDiffSplitter =
        if (inlineDiffByWord) SPLITTER_BY_WORD
        else SPLITTER_BY_CHARACTER
      this
    }

    /** To provide some customized splitting a splitter can be provided. Here
      * someone could think about sentence splitter, comma splitter or stuff
      * like that.
      *
      * @param inlineDiffSplitter
      * @return
      */
    def inlineDiffBySplitter(inlineDiffSplitter: Function[String, util.List[String]]): DiffRowGenerator.Builder = {
      this.inlineDiffSplitter = inlineDiffSplitter
      this
    }

    /** By default DiffRowGenerator preprocesses lines for HTML output. Tabs
      * and special HTML characters like "&lt;" are replaced with its encoded
      * value. To change this you can provide a customized line normalizer
      * here.
      *
      * @param lineNormalizer
      * @return
      */
    def lineNormalizer(lineNormalizer: Function[String, String]): DiffRowGenerator.Builder = {
      this.lineNormalizer = lineNormalizer
      this
    }

    /** Provide an equalizer for diff processing.
      *
      * @param equalizer equalizer for diff processing.
      * @return builder with configured equalizer parameter
      */
    def equalizer(equalizer: Function[String, String]): DiffRowGenerator.Builder = {
      this.equalizer = equalizer
      this
    }

    /** Sometimes it happens that a change contains multiple lines. If there
      * is no correspondence in old and new. To keep the merged line more
      * readable the linefeeds could be replaced by spaces.
      *
      * @param replace
      * @return
      */
    def replaceOriginalLinefeedInChangesWithSpaces(replace: Boolean): DiffRowGenerator.Builder = {
      this.replaceOriginalLinefeedInChangesWithSpaces = replace
      this
    }
  }
}

final class DiffRowGenerator private (val builder: DiffRowGenerator.Builder) {
  private var columnWidth = builder.columnWidth
  private var equalizer = builder.equalizer
  private var ignoreWhiteSpaces = builder.ignoreWhiteSpaces
  private var inlineDiffSplitter = builder.inlineDiffSplitter
  private var mergeOriginalRevised = builder.mergeOriginalRevised
  private var reportLinesUnchanged = builder.reportLinesUnchanged
  private var lineNormalizer = builder.lineNormalizer
  private var processDiffs = builder.processDiffs
  private var showInlineDiffs = builder.showInlineDiffs
  private var replaceOriginalLinefeedInChangesWithSpaces = builder.replaceOriginalLinefeedInChangesWithSpaces

  /** Get the DiffRows describing the difference between original and revised
    * texts using the given patch. Useful for displaying side-by-side diff.
    *
    * @param original the original text
    * @param revised  the revised text
    * @return the DiffRows between original and revised texts
    */
  def generateDiffRows(original: util.List[String], revised: util.List[String]): util.List[DiffRow] =
    generateDiffRows(original, DiffUtils.diff(original, revised))

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
    import scala.collection.JavaConversions._
    for (originalDelta <- deltaList) {
      for (delta <- decompressDeltas(originalDelta)) {
        endPos = transformDeltaIntoDiffRow(original, endPos, diffRows, delta)
      }
    }
    // Copy the final matching chunk if any.
    for (line <- original.subList(endPos, original.size)) {
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
    import scala.collection.JavaConversions._
    for (line <- original.subList(endPos, orig.getPosition)) {
      diffRows.add(buildDiffRow(DiffRow.Tag.EQUAL, line, line))
    }
    delta.getType match {
      case Delta.TYPE.INSERT =>
        import scala.collection.JavaConversions._
        for (line <- rev.getLines) {
          diffRows.add(buildDiffRow(DiffRow.Tag.INSERT, "", line))
        }

      case Delta.TYPE.DELETE =>
        import scala.collection.JavaConversions._
        for (line <- orig.getLines) {
          diffRows.add(buildDiffRow(DiffRow.Tag.DELETE, line, ""))
        }

      case _ =>
        if (showInlineDiffs) diffRows.addAll(generateInlineDiffs(delta))
        else
          for (j <- 0 until Math.max(orig.size, rev.size)) {
            diffRows.add(
              buildDiffRow(
                DiffRow.Tag.CHANGE,
                if (orig.getLines.size > j) orig.getLines.get(j)
                else "",
                if (rev.getLines.size > j) rev.getLines.get(j)
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
      //System.out.println("decompress this " + delta);
      val minSize = Math.min(delta.getSource.size, delta.getTarget.size)
      val orig = delta.getSource
      val rev = delta.getTarget
      deltas.add(
        new ChangeDelta[String](
          new Chunk[String](orig.getPosition, orig.getLines.subList(0, minSize)),
          new Chunk[String](rev.getPosition, rev.getLines.subList(0, minSize))
        )
      )
      if (orig.getLines.size < rev.getLines.size)
        deltas.add(
          new InsertDelta[String](
            new Chunk[String](orig.getPosition + minSize, Collections.emptyList[String]),
            new Chunk[String](rev.getPosition + minSize, rev.getLines.subList(minSize, rev.getLines.size))
          )
        )
      else
        deltas.add(
          new DeleteDelta[String](
            new Chunk[String](orig.getPosition + minSize, orig.getLines.subList(minSize, orig.getLines.size)),
            new Chunk[String](rev.getPosition + minSize, Collections.emptyList[String])
          )
        )
      return deltas
    }
    Collections.singletonList(delta)
  }

  private def buildDiffRow(`type`: DiffRow.Tag, orgline: String, newline: String) = if (reportLinesUnchanged)
    new DiffRow(`type`, orgline, newline)
  else {
    var wrapOrg = preprocessLine(orgline)
    var wrapNew = preprocessLine(newline)
    new DiffRow(`type`, wrapOrg, wrapNew)
  }

  private def buildDiffRowWithoutNormalizing(`type`: DiffRow.Tag, orgline: String, newline: String) =
    new DiffRow(`type`, StringUtils.wrapText(orgline, columnWidth), StringUtils.wrapText(newline, columnWidth))

  private def normalizeLines(list: util.List[String]) = list

  /** Add the inline diffs for given delta
    *
    * @param delta the given delta
    */
  private def generateInlineDiffs(delta: Delta[String]) = {
    val orig = normalizeLines(delta.getSource.getLines)
    val rev = normalizeLines(delta.getTarget.getLines)
    val joinedOrig = String.join("\n", orig)
    val joinedRev = String.join("\n", rev)
    var origList = inlineDiffSplitter.apply(joinedOrig)
    var revList = inlineDiffSplitter.apply(joinedRev)
    val inlineDeltas = DiffUtils.diff(origList, revList).getDeltas
    Collections.reverse(inlineDeltas)
    import scala.collection.JavaConversions._
    for (inlineDelta <- inlineDeltas) {
      val inlineOrig = inlineDelta.getSource
      val inlineRev = inlineDelta.getTarget
      if (inlineDelta.getType eq Delta.TYPE.DELETE) {
//        DiffRowGenerator.wrapInTag(
//          origList,
//          inlineOrig.getPosition,
//          inlineOrig.getPosition + inlineOrig.size,
//          DiffRow.Tag.DELETE,
//          oldTag,
//          processDiffs,
//          replaceOriginalLinefeedInChangesWithSpaces && mergeOriginalRevised
//        )
      } else if (inlineDelta.getType eq Delta.TYPE.INSERT) {
        if (mergeOriginalRevised) {
          origList.addAll(
            inlineOrig.getPosition,
            revList.subList(inlineRev.getPosition, inlineRev.getPosition + inlineRev.size)
          )
//          DiffRowGenerator.wrapInTag(
//            origList,
//            inlineOrig.getPosition,
//            inlineOrig.getPosition + inlineRev.size,
//            DiffRow.Tag.INSERT,
//            newTag,
//            processDiffs,
//            false
//          )
        } else {
//          DiffRowGenerator.wrapInTag(revList, inlineRev.getPosition, inlineRev.getPosition + inlineRev.size, DiffRow.Tag.INSERT, newTag, processDiffs, false)
        }
      } else if (inlineDelta.getType eq Delta.TYPE.CHANGE) {
        if (mergeOriginalRevised) {
          origList.addAll(
            inlineOrig.getPosition + inlineOrig.size,
            revList.subList(inlineRev.getPosition, inlineRev.getPosition + inlineRev.size)
          )
//          DiffRowGenerator.wrapInTag(
//            origList,
//            inlineOrig.getPosition + inlineOrig.size,
//            inlineOrig.getPosition + inlineOrig.size + inlineRev.size,
//            DiffRow.Tag.CHANGE,
//            newTag,
//            processDiffs,
//            false
//          )
        } else {
//          DiffRowGenerator.wrapInTag(
//            revList,
//            inlineRev.getPosition,
//            inlineRev.getPosition + inlineRev.size,
//            DiffRow.Tag.CHANGE,
//            newTag,
//            processDiffs,
//            false
//          )
        }
//        DiffRowGenerator.wrapInTag(
//          origList,
//          inlineOrig.getPosition,
//          inlineOrig.getPosition + inlineOrig.size,
//          DiffRow.Tag.CHANGE,
//          oldTag,
//          processDiffs,
//          replaceOriginalLinefeedInChangesWithSpaces && mergeOriginalRevised
//        )
      }
    }
    val origResult = new StringBuilder
    val revResult = new StringBuilder
    import scala.collection.JavaConversions._
    for (character <- origList) {
      origResult.append(character)
    }
    import scala.collection.JavaConversions._
    for (character <- revList) {
      revResult.append(character)
    }
    val original = origResult.toString.split("\n")
    val revised = revResult.toString.split("\n")
    val diffRows = new util.ArrayList[DiffRow]
    for (j <- 0 until Math.max(original.size, revised.size)) {
      diffRows.add(
        buildDiffRowWithoutNormalizing(
          DiffRow.Tag.CHANGE,
          if (original.size > j) original(j)
          else "",
          if (revised.size > j) revised(j)
          else ""
        )
      )
    }
    diffRows
  }

  private def preprocessLine(line: String) = if (columnWidth == 0) line
  else StringUtils.wrapText(line, columnWidth)
}
