# scalatest

To use with scalatest, add the following dependency:

## sbt

For use with `should` matchers:
```scala
"com.softwaremill.diffx" %% "diffx-scalatest-should" % "0.6.0" % Test
```

For use with `must` matchers:
```scala
"com.softwaremill.diffx" %% "diffx-scalatest-should" % "0.6.0" % Test
```

## mill

For use with `should` matchers:
```scala
ivy"com.softwaremill.diffx::diffx-scalatest-must::0.6.0"
```

For use with `must` matchers:
```scala
ivy"com.softwaremill.diffx::diffx-scalatest-must::0.6.0"
```

## Usage

Then, depending on the chosen matcher style extend or import relevant trait/object:
- should -> `com.softwaremill.diffx.scalatest.DiffShouldMatcher`
- must -> `com.softwaremill.diffx.scalatest.DiffMustMatcher`

After that you will be able to use syntax such as:

```scala
import com.softwaremill.diffx.scalatest.DiffShouldMatcher._
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

left shouldMatchTo(right)
```
