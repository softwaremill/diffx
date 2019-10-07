# diffx 
[![Build Status](https://travis-ci.org/softwaremill/diffx.svg?branch=master)](https://travis-ci.org/softwaremill/diffx)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.softwaremill.diffx/diffx-core_2.13/badge.svg)](https://search.maven.org/search?q=g:com.softwaremill.diffx)
[![Gitter](https://badges.gitter.im/softwaremill/diffx.svg)](https://gitter.im/softwaremill/diffx?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Mergify Status](https://img.shields.io/endpoint.svg?url=https://gh.mergify.io/badges/softwaremill/diffx&style=flat)](https://mergify.io)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-brightgreen.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

Pretty diffs for case classes. 

The library is published for Scala 2.12 and 2.13.

## Scalatest integration

To use with scalatest, add the following dependency:

```scala
"com.softwaremill.diffx" %% "diffx-scalatest" % "0.3.4"
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
"com.softwaremill.diffx" %% "diffx-core" % "0.3.4"
```

Then call:

```scala
import com.softwaremill.diffx.Diff

Diff[T].apply(o1, o2)
```

## Customization

If you'd like to implement custom matching logic for the given type, create an implicit `Diff` instance for that 
type, and make sure it's in scope when any any `Diff` instances depending on that type are created.


## Ignoring

Fields can be excluded from comparision by calling the `ignore` method on the `Diff` instance.
Since `Diff` instances are immutable, the `ignore` method creates a copy of the instance with modified logic.
You can use this instance explicitly.
If you still would like to use it implicitly, you first need to summon the instance of the `Diff` typeclass using
the `Derived` typeclass wrapper: `Derived[Diff[Person]]`. Thanks to that trick, later you will be able to put your modified
instance of the `Diff` typeclass into the implicit scope. The whole process looks as follows:

```scala
implicit modifiedDiff: Diff[Person] = Derived[Diff[Person]].ignore(_.name)
``` 

## Tagging support

Support for tagged types can be added easily by providing an additional generic instance of the `Diff` type class
```scala
implicit def taggedDiff[T:Diff, U]: Diff[T @@ U] = Diff[T].contramap(identity)
```

