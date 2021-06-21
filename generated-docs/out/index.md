# diffx: Pretty diffs for case classes

Welcome!

[diffx](https://github.com/softwaremill/diffx) is an open-source library which aims to display differences between 
complex structures in a way that they are easily noticeable.

Here's a quick example of diffx in action:

```scala
sealed trait Parent
case class Bar(s: String, i: Int) extends Parent
case class Foo(bar: Bar, b: List[Int], parent: Option[Parent]) extends Parent

val right: Foo = Foo(
    Bar("asdf", 5),
    List(123, 1234),
    Some(Bar("asdf", 5))
)
// right: Foo = Foo(
//   bar = Bar(s = "asdf", i = 5),
//   b = List(123, 1234),
//   parent = Some(value = Bar(s = "asdf", i = 5))
// )

val left: Foo = Foo(
    Bar("asdf", 66),
    List(1234),
    Some(right)
)
// left: Foo = Foo(
//   bar = Bar(s = "asdf", i = 66),
//   b = List(1234),
//   parent = Some(
//     value = Foo(
//       bar = Bar(s = "asdf", i = 5),
//       b = List(123, 1234),
//       parent = Some(value = Bar(s = "asdf", i = 5))
//     )
//   )
// )
 
import com.softwaremill.diffx.generic.auto._
import com.softwaremill.diffx._
compare(left, right)
// res0: DiffResult = DiffResultObject(
//   name = "Foo",
//   fields = ListMap(
//     "bar" -> DiffResultObject(
//       name = "Bar",
//       fields = ListMap(
//         "s" -> Identical(value = "asdf"),
//         "i" -> DiffResultValue(left = 66, right = 5)
//       )
//     ),
//     "b" -> DiffResultObject(
//       name = "List",
//       fields = ListMap(
//         "0" -> DiffResultValue(left = 1234, right = 123),
//         "1" -> DiffResultMissing(value = 1234)
//       )
//     ),
//     "parent" -> DiffResultValue(
//       left = "repl.MdocSession.App.Foo",
//       right = "repl.MdocSession.App.Bar"
//     )
//   )
// )
```

Will result in:

![](https://github.com/softwaremill/diffx/blob/master/example.png?raw=true)

`diffx` is available for Scala 2.12 and 2.13 both jvm and js.

The core of `diffx` comes in a single jar.

To integrate with the test framework of your choice, you'll need to use one of the integration modules.
See the section on [test-frameworks](test-frameworks/summary.md) for a brief overview of supported test frameworks.

## Sponsors

Development and maintenance of sttp client is sponsored by [SoftwareMill](https://softwaremill.com), a software development and consulting company. We help clients scale their business through software. Our areas of expertise include backends, distributed systems, blockchain, machine learning and data analytics.

[![](https://files.softwaremill.com/logo/logo.png "SoftwareMill")](https://softwaremill.com)

# Table of contents

```eval_rst
.. toctree::
   :maxdepth: 1
   :caption: Test frameworks
   
   test-frameworks/scalatest
   test-frameworks/specs2
   test-frameworks/utest
   
.. toctree::
   :maxdepth: 2
   :caption: Integrations
   
   integrations/cats
   integrations/tagging
   integrations/refined
```
