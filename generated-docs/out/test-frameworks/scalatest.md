# scalatest

To use with scalatest, add the following dependency:

## sbt

```scala
"com.softwaremill.diffx" %% "diffx-scalatest" % "0.5.0" % Test
```

## mill

```scala
ivy"com.softwaremill.diffx::diffx-scalatest::0.5.0"
```

## Usage

Then, extend the `com.softwaremill.diffx.scalatest.DiffMatcher` trait or `import com.softwaremill.diffx.scalatest.DiffMatcher._`.
After that you will be able to use syntax such as:

```scala
import org.scalatest.matchers.should.Matchers._
import com.softwaremill.diffx.scalatest.DiffMatcher._
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

left should matchTo(right)
```

###### TODO add info about misleading compilation error when using above syntax