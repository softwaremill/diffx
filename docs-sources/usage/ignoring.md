# ignoring

```scala mdoc:invisible
import com.softwaremill.diffx.generic.auto._
import com.softwaremill.diffx._
```

Fields can be excluded from comparison by calling the `ignore` method on the `Diff` instance.
Since `Diff` instances are immutable, the `ignore` method creates a copy of the instance with modified logic.
You can use this instance explicitly.

```scala mdoc:compile-only
case class Person(name:String, age:Int)
val modifiedDiff: Diff[Person] = Diff[Person].ignore(_.name)
```

If you still would like to use it implicitly, you first need to summon the instance of the `Diff` typeclass using
the `Derived` typeclass wrapper: `Derived[Diff[Person]]`. Thanks to that trick, later you will be able to put your modified
instance of the `Diff` typeclass into the implicit scope. The whole process looks as follows:

```scala mdoc:compile-only
case class Person(name:String, age:Int)
implicit val modifiedDiff: Diff[Person] = Derived[Diff[Person]].ignore(_.name)
``` 

Ignoring is implemented using the replacement mechanic, head over to [replacing](replacing.md) section for more details.