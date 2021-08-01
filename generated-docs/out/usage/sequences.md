# sequences

`diffx` provides instances for many containers from scala's standard library (e.g. lists, sets, maps), however 
not all collections can be simply compared. Ordered collections like lists or vectors are compared by default by 
comparing elements under the same indexes. 
On the other hand maps, by default, are compared by comparing values under the respective keys. 
For unordered collections there is an `ObjectMapper` typeclass which defines how elements should be paired. 

## object matcher

In general, it is a very simple interface, with a bunch of factory methods.
```scala
trait ObjectMatcher[T] {
  def isSameObject(left: T, right: T): Boolean
}
```

It is mostly useful when comparing unordered collections like sets:

```scala
import com.softwaremill.diffx._
import com.softwaremill.diffx.generic.auto._

case class Person(id: String, name: String)

implicit val personMatcher = ObjectMatcher.set[Person].by(_.id)
val bob = Person("1","Bob") 
```
```scala
compare(Set(bob), Set(bob, Person("2","Alice")))
// res1: DiffResult = DiffResultSet(
//   diffs = Set(
//     DiffResultObject(
//       name = "Person",
//       fields = ListMap(
//         "id" -> IdenticalValue(value = "1"),
//         "name" -> IdenticalValue(value = "Bob")
//       )
//     ),
//     DiffResultMissing(value = Person(id = "2", name = "Alice"))
//   )
// )
```

It can be also used to modify how the entries from maps are paired.
In below example we tell `diffx` to compare these maps by paring entries by values using the defined `personMatcher`
```scala
import com.softwaremill.diffx._
import com.softwaremill.diffx.generic.auto._

case class Person(id: String, name: String)

implicit val om = ObjectMatcher.map[String, Person].byValue(_.id)
val bob = Person("1","Bob")
```

```scala
compare(Map("1" -> bob), Map("2" -> bob))
// res3: DiffResult = DiffResultMap(
//   entries = Map(
//     DiffResultString(
//       diffs = List(
//         DiffResultStringLine(
//           diffs = List(DiffResultValue(left = "1", right = "2"))
//         )
//       )
//     ) -> DiffResultObject(
//       name = "Person",
//       fields = ListMap(
//         "id" -> IdenticalValue(value = "1"),
//         "name" -> IdenticalValue(value = "Bob")
//       )
//     )
//   )
// )
```

Last but not least you can use `objectMatcher` to customize paring when comparing indexed collections.
Such collections are treated similarly to maps (they use key-value object matcher),
but the key type is bound to `Int` (`IterableEntry` is an alias for `MapEntry[Int,V]`).

```scala
import com.softwaremill.diffx._
import com.softwaremill.diffx.generic.auto._

case class Person(id: String, name: String)

implicit val personMatcher = ObjectMatcher.list[Person].byValue(_.id)
val bob = Person("1","Bob")
val alice = Person("2","Alice")
```
```scala
compare(List(bob, alice), List(alice, bob))
// res5: DiffResult = DiffResultObject(
//   name = "List",
//   fields = ListMap(
//     "0" -> DiffResultObject(
//       name = "Person",
//       fields = ListMap(
//         "id" -> IdenticalValue(value = "2"),
//         "name" -> IdenticalValue(value = "Alice")
//       )
//     ),
//     "1" -> DiffResultObject(
//       name = "Person",
//       fields = ListMap(
//         "id" -> IdenticalValue(value = "1"),
//         "name" -> IdenticalValue(value = "Bob")
//       )
//     )
//   )
// )
```

*Note: `ObjectMatcher` can be also passed explicitly, either upon creation or during modification*
*See [replacing](replacing.md) for details.*