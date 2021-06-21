# cats

This module contains integration layer between [org.typelevel.cats](https://github.com/typelevel/cats) and `diffx`

## sbt

```scala
"com.softwaremill.diffx" %% "diffx-cats" % "0.5.0+13-3ec13ae6+20210621-1024-SNAPSHOT" % Test    
```

## mill

```scala
ivy"com.softwaremill.diffx::diffx-cats::0.5.0+13-3ec13ae6+20210621-1024-SNAPSHOT"
```

## Usage

Assuming you have some data types from the cats library in your hierarchy:
```scala
import cats.data._
case class TestData(ints: NonEmptyList[String])

val t1 = TestData(NonEmptyList.one("a"))
// t1: TestData = TestData(ints = NonEmptyList(head = "a", tail = List()))
val t2 = TestData(NonEmptyList.one("b"))
// t2: TestData = TestData(ints = NonEmptyList(head = "b", tail = List()))
```

all you need to do is to put additional diffx implicits into current scope:

```scala
import com.softwaremill.diffx.compare
import com.softwaremill.diffx.generic.auto._

import com.softwaremill.diffx.cats._
compare(t1, t2)
// res0: com.softwaremill.diffx.DiffResult = DiffResultObject(
//   name = "TestData",
//   fields = ListMap(
//     "ints" -> DiffResultObject(
//       name = "List",
//       fields = ListMap(
//         "0" -> DiffResultString(diffs = List(DiffResultValue(left = "a", right = "b")))
//       )
//     )
//   )
// )
```