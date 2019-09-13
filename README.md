# diffx 
[![Build Status](https://travis-ci.org/softwaremill/diffx.svg?branch=master)](https://travis-ci.org/softwaremill/diffx)

Pretty diffs for case classes. 

## Scalatest integration

To use with scalatest, add the following dependency:

```scala
"com.softwaremill.diffx" %% "diffx-scalatest" % "0.1.1"
```

Then, extend the `com.softwaremill.diffx.scalatest.DiffMatcher` trait or `import com.softwaremill.diffx.scalatest.DiffMatcher._`.
After that you will be able to use syntax such as:

```scala
left should matchTo(right)
```

Giving you nice error messages:

![example](https://github.com/softwaremill/diff-x/blob/master/example.png?raw=true)

## Using directly

Add the following dependency:

```scala
"com.softwaremill.diffx" %% "diffx-core" % "0.1.1"
```

Then call:

```scala
import com.softwaremill.diffx.Diff
Diff[T].diff(o1, o2)
```

## Customization

If you'd like to implement custom matching logic for given type, create an implicit `Diff` instance for that 
type, and make sure it's in scope when creating `Diff` for the root type.


## Ignoring

Fields can be excluded from comparision simply by calling `ignore` method on `Diff` instance.
Since `Diff` instances are immutable it creates a copy with modified logic. You can use this instance 
explicitly. If you still would like to use it implicitly you first need to summon instance of `Diff` typeclass using
`Derived` typeclass wrapper: `Derived[Diff[Person]]`. Thanks to that trick latter you will be able to put your modified
instance of `Diff` typeclass into implicit scope. The whole process looks following:
```scala
implicit modifiedDiff: Diff[Person] = Derived[Diff[Person]].ignore(_.name)
``` 

