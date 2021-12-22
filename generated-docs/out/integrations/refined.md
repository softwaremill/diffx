# refined

This module contains integration layer between [eu.timepit.refined](https://github.com/fthomas/refined) and `diffx`

## sbt

```scala
"com.softwaremill.diffx" %% "diffx-refined" % "0.7.0" % Test    
```

## mill

```scala
ivy"com.softwaremill.diffx::diffx-refined::0.7.0"
```

## Usage

Assuming you have some refined types in your hierarchy:

```scala
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.auto._
import eu.timepit.refined.types.string.NonEmptyString

case class TestData(posInt: PosInt, nonEmptyString: NonEmptyString)

val t1 = TestData(1, "foo")
val t2 = TestData(1, "bar")
```

all you need to do is to put additional diffx implicits into current scope:

```scala
import com.softwaremill.diffx.compare
import com.softwaremill.diffx.generic.auto._

import com.softwaremill.diffx.refined._
compare(t1, t2)
// res0: com.softwaremill.diffx.DiffResult = DiffResultObject(
//   name = "TestData",
//   fields = ListMap(
//     "posInt" -> IdenticalValue(value = 1),
//     "nonEmptyString" -> DiffResultString(
//       diffs = List(
//         DiffResultStringLine(
//           diffs = List(DiffResultValue(left = "foo", right = "bar"))
//         )
//       )
//     )
//   )
// )
```