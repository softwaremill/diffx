# weaver

To use with weaver, add the following dependency:

## sbt

```scala
"com.softwaremill.weaver" %% "diffx-weaver" % "0.9.0" % Test
```

## mill

```scala
ivy"com.softwaremill.diffx::diffx-weaver::0.9.0"
```

## Usage

Then, mixin `DiffxExpectations` trait or add `import com.softwaremill.diffx.weaver.DiffxExpectations._` to your test code.
To assert using diffx use `expectEqual` as follows:

```scala
import com.softwaremill.diffx.weaver.DiffxExpectations._
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

expectEqual(left, right)
```

## Versions matrix

Below table shows past Diffx releases with the corresponding weaver version they were build with.
For newer versions checkout the release changelog.

| Diffx | weaver |
| ----- | :----: |
| 0.9.0 | 0.8.3  |
