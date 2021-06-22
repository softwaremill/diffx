# cats

This module contains integration layer between [org.typelevel.cats](https://github.com/typelevel/cats) and `diffx`

## sbt

```scala
"com.softwaremill.diffx" %% "diffx-cats" % "@VERSION@" % Test    
```

## mill

```scala
ivy"com.softwaremill.diffx::diffx-cats::@VERSION@"
```

## Usage

Assuming you have some data types from the cats library in your hierarchy:
```scala mdoc:silent
import cats.data._
case class TestData(ints: NonEmptyList[String])

val t1 = TestData(NonEmptyList.one("a"))
val t2 = TestData(NonEmptyList.one("b"))
```

all you need to do is to put additional diffx implicits into current scope:

```scala mdoc
import com.softwaremill.diffx.compare
import com.softwaremill.diffx.generic.auto._

import com.softwaremill.diffx.cats._
compare(t1, t2)
```