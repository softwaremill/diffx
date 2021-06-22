# sequences

`diffx` provides instances for many containers from scala's standard library (e.g. lists, sets, maps), however 
not all collections can be simply compared. Ordered collections like lists or vectors are compared by default by 
comparing elements under the same indexes. 
Maps, by default, are compared by comparing values under the respective keys. 
For unordered collections there is an `ObjectMapper` typeclass which defines how elements should be paired. 

## object matcher

In general, it is a very simple interface, with a bunch of factory methods.
```scala mdoc:compile-only
trait ObjectMatcher[T] {
  def isSameObject(left: T, right: T): Boolean
}
```

It is mostly useful when comparing unordered collections like sets:

```scala mdoc:silent
import com.softwaremill.diffx._
import com.softwaremill.diffx.generic.auto._
case class Person(id: String, name: String)

implicit val personMatcher: ObjectMatcher[Person] = ObjectMatcher.by(_.id)
val bob = Person("1","Bob") 
```
```scala mdoc
compare(Set(bob), Set(bob, Person("2","Alice")))
```

It can be also used to modify how the entries from maps are paired.
In below example we tell `diffx` to compare these maps by paring entries by values using the defined `personMatcher`
```scala mdoc:reset:silent
import com.softwaremill.diffx._
import com.softwaremill.diffx.generic.auto._
case class Person(id: String, name: String)

val personMatcher: ObjectMatcher[Person] = ObjectMatcher.by(_.id)
implicit val om: ObjectMatcher[(String, Person)] = ObjectMatcher.byValue(personMatcher)
val bob = Person("1","Bob")
```

```scala mdoc
compare(Map("1" -> bob), Map("2" -> bob))
```

Last but not least you can use `objectMatcher` to customize paring when comparing indexed collections.
Such collections are treated similarly to maps (they use key-value object matcher),
but the key type is bound to `Int`.

```scala mdoc:reset:silent
import com.softwaremill.diffx._
import com.softwaremill.diffx.generic.auto._
case class Person(id: String, name: String)

implicit val personMatcher: ObjectMatcher[(Int, Person)] = 
    ObjectMatcher.byValue(ObjectMatcher.by(_.id))
val bob = Person("1","Bob")
val alice = Person("2","Alice")
```
```scala mdoc
compare(List(bob, alice), List(alice, bob))
```

*Note: `ObjectMatcher` can be also passed explicitly, either upon creation or during modification*