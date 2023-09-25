# ignoring


Fields can be excluded from comparison by calling the `ignore` method on the `Diff` instance.
Since `Diff` instances are immutable, the `ignore` method creates a copy of the instance with modified logic.
You can use this instance explicitly.

```scala
case class Person(name:String, age:Int)
val modifiedDiff: Diff[Person] = Diff[Person].ignore(_.name)
```

If you still would like to use it implicitly, you first need to summon the instance of the `Diff` typeclass using
the `Derived` typeclass wrapper: `Derived[Diff[Person]]`. Thanks to that trick, later you will be able to put your modified
instance of the `Diff` typeclass into the implicit scope. The whole process looks as follows:

```scala
case class Person(name:String, age:Int)
implicit val modifiedDiff: Diff[Person] = Diff.derived[Person].ignore(_.age)
```
```scala
compare(Person("bob", 25), Person("bob", 30))
// res1: DiffResult = DiffResultObject(
//   name = "Person",
//   fields = ListMap(
//     "name" -> IdenticalValue(value = "bob"),
//     "age" -> IdenticalValue(value = "<ignored>")
//   )
// )
```

Starting from `diffx` 0.5.5 it is possible to globally customize how ignoring works. By default, an instance of
`Diff` under a particular path gets replaced by `Diff.ignored` instance. `Diff.ignored` is configured to always produce 
identical results with fixed placeholder `"<ignored>"` no-matter what it gets. To customize that behavior one has to 
create an implicit instance of `DiffConfiguration` with desired behavior. Below is an example of how to include results of 
original comparison into ignored output:

```scala
implicit val conf: DiffConfiguration = DiffConfiguration(makeIgnored =
  (original: Diff[Any]) =>
    (left: Any, right: Any, context: DiffContext) => {
      IdenticalValue(s"Ignored but was: ${original.apply(left, right, context).show()(ShowConfig.noColors)}")
    }
)
// conf: DiffConfiguration = DiffConfiguration(makeIgnored = <function1>)
val d = Diff[Person].ignore(_.age)
// d: Diff[Person] = com.softwaremill.diffx.Diff$$anon$1@5a5ef32b
d(Person("bob", 25), Person("bob", 30)) 
// res2: DiffResult = DiffResultObject(
//   name = "Person",
//   fields = ListMap(
//     "name" -> IdenticalValue(value = "bob"),
//     "age" -> IdenticalValue(value = "<ignored>")
//   )
// )
```