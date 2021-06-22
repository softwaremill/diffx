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
implicit val modifiedDiff: Diff[Person] = Derived[Diff[Person]].ignore(_.age)
// modifiedDiff: Diff[Person] = com.softwaremill.diffx.Diff$$anon$1@4d9d512a

compare(Person("bob", 25), Person("bob", 30))
// res1: DiffResult = Identical(value = Person(name = "bob", age = 25))
```