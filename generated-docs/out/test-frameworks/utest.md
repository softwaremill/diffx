# utest

To use with utest, add following dependency:

## sbt

```scala
"com.softwaremill.diffx" %% "diffx-utest" % "0.8.3" % Test
```

## mill

```scala
ivy"com.softwaremill.diffx::diffx-utest::0.8.3"
```

## Usage

Then, mixin `DiffxAssertions` trait or add `import com.softwaremill.diffx.utest.DiffxAssertions._` to your test code.
To assert using diffx use `assertEquals` as follows:

```scala
import com.softwaremill.diffx.utest.DiffxAssertions._
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

## Versions matrix

Below table shows past Diffx releases with the corresponding utest version they were build with.
For newer versions checkout the release changelog.

| Diffx  | scalatest |
|--------|:---------:|
| 0.7.0  |  0.7.10   |
| 0.6.0  |  0.7.10   |
| 0.5.x  |  0.7.10   |
| 0.4.5  |   0.7.7   |
| 0.4.4  |   0.7.7   |
| 0.4.3  |   0.7.7   |
| 0.4.2  |   0.7.7   |
| 0.4.1  |   0.7.7   |
| 0.4.0  |   0.7.5   |
| 0.3.30 |   0.7.5   |
| 0.3.29 |   0.7.4   |
| 0.3.28 |   0.7.4   |

