# diff-x 
[![Build Status](https://travis-ci.org/softwaremill/diff-x.svg?branch=master)](https://travis-ci.org/softwaremill/diff-x)

Pretty diffs for case classes. 

## Scalatest integration

To use with scalatest, add the following dependency:

```scala
"com.softwaremill.diffx" %% "diffx-scalatest" % "0.1.1"
```

Then, extend the `com.softwaremill.diffx.DiffMatcher` trait or `import com.softwaremill.diffx.DiffMatcher._`.
You will then be able to use syntax such as:

```scala
left should matchTo(right)
```

Giving you nice error messages, such as:

![example](https://github.com/softwaremill/diff-x/blob/master/example.png?raw=true)

## Using directly

Add the following dependency:

```scala
"com.softwaremill.diffx" %% "diffx-core" % "0.1.1"
```

Then call:

```scala
import com.softwaremill.diffx.DiffFor
implicitly[DiffFor[T]].diff(o1, o2)
```

## Custom types

If you'd like to implement custom matching logic for a custom type, create an implicit `DiffFor` instance for that 
type, and make sure it's in scope when creating `DiffFor` for the case class.
