# specs2

To use with specs2, add the following dependency:

## sbt

```scala
"com.softwaremill.diffx" %% "diffx-specs2" % "0.5.4" % Test
```

## mill

```scala
ivy"com.softwaremill.diffx::diffx-specs2::0.5.4"
```

## Usage

Then, extend the `com.softwaremill.diffx.specs2.DiffMatcher` trait or `import com.softwaremill.diffx.specs2.DiffMatcher._`.
After that you will be able to use syntax such as:

```scala
import org.specs2.matcher.MustMatchers.{left => _, right => _, _}
import com.softwaremill.diffx.specs2.DiffMatcher._
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

left must matchTo(right)
```
