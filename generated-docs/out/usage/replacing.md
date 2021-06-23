# replacing

Sometimes you might want to compare some nested values using a different comparator but
the type they share is not unique within that hierarchy.

Consider following example:
```scala
case class Person(age: Int, weight: Int)
```

If we would like to compare `weight` differently than `age` we would have to introduce a new type for `weight` 
in order to provide a different `Diff` typeclass for only that field. While in general, it is a good idea to have your types 
very precise it might not always be practical or even possible. Fortunately, diffx comes with a mechanism which allows
the replacement of nested diff instances.

First we need to acquire a lens at given path using `modify` method, 
and then we can call `setTo` to replace a particular instance.

```scala
import com.softwaremill.diffx._
implicit val diffPerson: Derived[Diff[Person]] = Diff.derived[Person].modify(_.weight)
        .setTo(Diff.approximate(epsilon=5))
```

```scala
compare(Person(23, 60), Person(23, 62))
// res0: DiffResult = DiffResultObject(
//   name = "Person",
//   fields = ListMap(
//     "age" -> IdenticalValue(value = 23),
//     "weight" -> IdenticalValue(value = 60)
//   )
// )
```

In fact, replacement is so powerful that ignoring is implemented as a replacement 
with the `Diff.ignore` instance.

You can use the same mechanism to set particular object matcher for given nested collection in the hierarchy.
Depending, whether it is list, set or map a respective method needs to be called:
```scala
case class Organization(peopleList: List[Person], peopleSet: Set[Person], peopleMap: Map[String, Person])
implicit val diffOrg: Derived[Diff[Organization]] = Diff.derived[Organization]
        .modify(_.peopleList).withListMatcher[Person](ObjectMatcher.byValue(_.age))
        .modify(_.peopleSet).withSetMatcher[Person](ObjectMatcher.by(_.age))
        .modify(_.peopleMap).withMapMatcher[String,Person](ObjectMatcher.byValue(_.age))
```