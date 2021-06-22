# tagging

This module contains integration layer between [com.softwaremill.common.tagging](https://github.com/softwaremill/scala-common) and `diffx`

## sbt

```scala
"com.softwaremill.diffx" %% "diffx-tagging" % "0.5.0+13-3ec13ae6+20210621-1024-SNAPSHOT"
```

## mill 

```scala
ivy"com.softwaremill.diffx::diffx-tagging::0.5.0+13-3ec13ae6+20210621-1024-SNAPSHOT"
```

## Usage

Assuming you have some tagged types in your hierarchy:

```scala
import com.softwaremill.tagging._
sealed trait T1
sealed trait T2
case class TestData(p1: Int @@ T1, p2: Int @@ T2)

val t1 = TestData(1.taggedWith[T1], 1.taggedWith[T2])
// t1: TestData = TestData(p1 = 1, p2 = 1)
val t2 = TestData(1.taggedWith[T1], 3.taggedWith[T2])
// t2: TestData = TestData(p1 = 1, p2 = 3)
```

all you need to do is to put additional diffx implicits into current scope:

```scala
import com.softwaremill.diffx.compare
import com.softwaremill.diffx.generic.auto._

import com.softwaremill.diffx.tagging._
compare(t1, t2)
// res0: com.softwaremill.diffx.DiffResult = DiffResultObject(
//   name = "TestData",
//   fields = ListMap(
//     "p1" -> Identical(value = 1),
//     "p2" -> DiffResultValue(left = 1, right = 3)
//   )
// )
```