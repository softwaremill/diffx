# specs2

To use with specs2, add the following dependency:

## sbt

```scala
"com.softwaremill.diffx" %% "diffx-specs2" % "0.9.1" % Test
```

## mill

```scala
ivy"com.softwaremill.diffx::diffx-specs2::0.9.1"
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

## Versions matrix

Below table shows past Diffx releases with the corresponding specs2 version they were build with.
For newer versions checkout the release changelog.

| Diffx  |  scalatest   |
|--------|:------------:|
| 0.7.0  |    4.13.1    |
| 0.6.0  |    4.13.0    |
| 0.5.6  | 4.12.4-js-ec |
| 0.5.5  | 4.12.4-js-ec |
| 0.5.4  |    4.12.3    |
| 0.5.3  |    4.12.1    |
| 0.5.2  |    4.12.1    |
| 0.5.1  |    4.12.1    |
| 0.5.0  |    4.12.1    |
| 0.4.5  |    4.10.6    |
| 0.4.4  |    4.10.6    |
| 0.4.3  |    4.10.6    |
| 0.4.2  |    4.10.6    |
| 0.4.1  |    4.10.6    |
| 0.4.0  |    4.10.5    |
| 0.3.30 |    4.10.5    |
| 0.3.29 |    4.9.4     |
| 0.3.28 |    4.9.3     |

