# munit

To use with munit, add following dependency:

## sbt

```scala
"com.softwaremill.diffx" %% "diffx-munit" % "0.5.1" % Test
```

## mill

```scala
ivy"com.softwaremill.diffx::diffx-munit::0.5.1"
```

## Usage

Then, mixin `DiffxAssertions` trait or add `import com.softwaremill.diffx.munit.DiffxAssertions._` to your test code.
To assert using diffx use `assertEquals` as follows:

```scala
import com.softwaremill.diffx.munit.DiffxAssertions._
import com.softwaremill.diffx.generic.auto._

sealed trait Parent
case class Bar(s: String, i: Int) extends Parent
case class Foo(bar: Bar, b: List[Int], parent: Option[Parent]) extends Parent

val right: Foo = Foo(
    Bar("asdf", 5),
    List(123, 1234),
    Some(Bar("asdf", 5))
)

val left: Foo = Foo(
    Bar("asdf", 66),
    List(1234),
    Some(right)
)

assertEqual(left, right)
```

