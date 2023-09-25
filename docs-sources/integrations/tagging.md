# tagging

This module contains integration layer between [com.softwaremill.common.tagging](https://github.com/softwaremill/scala-common) and `diffx`

## sbt

```scala
"com.softwaremill.diffx" %% "diffx-tagging" % "@VERSION@"
```

## mill

```scala
ivy"com.softwaremill.diffx::diffx-tagging::@VERSION@"
```

## Usage

Assuming you have some tagged types in your hierarchy:

```scala mdoc:silent
import com.softwaremill.tagging._
sealed trait T1
sealed trait T2
case class TestData(p1: Int @@ T1, p2: Int @@ T2)

val t1 = TestData(1.taggedWith[T1], 1.taggedWith[T2])
val t2 = TestData(1.taggedWith[T1], 3.taggedWith[T2])
```

all you need to do is to put additional diffx implicits into current scope:

```scala mdoc
import com.softwaremill.diffx.compare
import com.softwaremill.diffx.generic.auto._

import com.softwaremill.diffx.tagging._
compare(t1, t2)
```
