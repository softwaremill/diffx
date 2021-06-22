# refined

This module contains integration layer between [eu.timepit.refined](https://github.com/fthomas/refined) and `diffx`

## sbt

```scala
"com.softwaremill.diffx" %% "diffx-refined" % "@VERSION@" % Test    
```

## mill

```scala
ivy"com.softwaremill.diffx::diffx-refined::@VERSION@"
```

## Usage

Assuming you have some refined types in your hierarchy:

```scala mdoc:silent
import eu.timepit.refined.types.numeric.PosInt
import eu.timepit.refined.auto._
import eu.timepit.refined.types.string.NonEmptyString

case class TestData(posInt: PosInt, nonEmptyString: NonEmptyString)

val t1 = TestData(1, "foo")
val t2 = TestData(1, "bar")
```

all you need to do is to put additional diffx implicits into current scope:

```scala mdoc
import com.softwaremill.diffx.compare
import com.softwaremill.diffx.generic.auto._

import com.softwaremill.diffx.refined._
compare(t1, t2)
```