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

![](https://github.com/softwaremill/diffx/blob/master/example.png?raw=true)

`diffx` is available for Scala 2.12 and 2.13 both jvm and js.

The core of `diffx` comes in a single jar.

To integrate with the test framework of your choice, you'll need to use one of the integration modules.
See the section on [test-frameworks](test-frameworks/summary.md) for a brief overview of supported test frameworks.

*Auto-derivation is used throughout the documentation for the sake of clarity. Head over to [derivation](usage/derivation.md) for more details*

## Tips and tricks

You may need to add `-Wmacros:after` Scala compiler option to make sure to check for unused implicits
after macro expansion.
If you get warnings from Magnolia which looks like `magnolia: using fallback derivation for TYPE`,
you can use the [Silencer](https://github.com/ghik/silencer) compiler plugin to silent the warning
with the compiler option `"-P:silencer:globalFilters=^magnolia: using fallback derivation.*$"`

## Similar projects

There is a number of similar projects from which diffx draws inspiration.

Below is a list of some of them, which I am aware of, with their main differences:
- [xotai/diff](https://github.com/xdotai/diff) - based on shapeless, seems not to be activly developed anymore
- [ratatool-diffy](https://github.com/spotify/ratatool/tree/master/ratatool-diffy) - the main purpose is to compare large data sets stored on gs or hdfs


## Sponsors

Development and maintenance of diffx is sponsored by [SoftwareMill](https://softwaremill.com), 
a software development and consulting company. We help clients scale their business through software. Our areas of expertise include backends, distributed systems, blockchain, machine learning and data analytics.

[![](https://files.softwaremill.com/logo/logo.png "SoftwareMill")](https://softwaremill.com)

# Table of contents

```eval_rst
.. toctree::
   :maxdepth: 1
   :caption: Test frameworks
   
   test-frameworks/scalatest
   test-frameworks/specs2
   test-frameworks/utest
   test-frameworks/summary
   
.. toctree::
   :maxdepth: 1
   :caption: Integrations
   
   integrations/cats
   integrations/tagging
   integrations/refined
   
.. toctree::
   :maxdepth: 1
   :caption: usage
   
   usage/derivation
   usage/ignoring
   usage/replacing
   usage/extending
```

## Copyright

Copyright (C) 2019 SoftwareMill [https://softwaremill.com](https://softwaremill.com).
