# diffx: Pretty diffs for case classes

Welcome!

[diffx](https://github.com/softwaremill/diffx) is an open-source library which aims to display differences between 
complex structures in a way that they are easily noticeable. 
 

Here's a quick example of diffx in action:

```scala mdoc
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
 
import com.softwaremill.diffx.generic.auto._
import com.softwaremill.diffx._
compare(left, right)
```

Will result in:

![example](https://github.com/softwaremill/diff-x/blob/master/example.png?raw=true)


## Sponsors

Development and maintenance of sttp client is sponsored by [SoftwareMill](https://softwaremill.com), a software development and consulting company. We help clients scale their business through software. Our areas of expertise include backends, distributed systems, blockchain, machine learning and data analytics.

[![](https://files.softwaremill.com/logo/logo.png "SoftwareMill")](https://softwaremill.com)

# Table of contents