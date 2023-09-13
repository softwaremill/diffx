# modifying

Sometimes you might want to compare some nested values using a different comparator but
the type they share is not unique within that hierarchy.

Consider following example:
```scala mdoc
import com.softwaremill.diffx._
import com.softwaremill.diffx.generic.auto._

case class Person(age: Int, weight: Int)
```

If we would like to compare `weight` differently than `age` we would have to introduce a new type for `weight` 
in order to provide a different `Diff` typeclass for only that field. While in general, it is a good idea to have your types 
very precise it might not always be practical or even possible. Fortunately, diffx comes with a mechanism which allows
the replacement of nested diff instances.

First we need to acquire a lens at given path using `modify` method, 
and then we can call `setTo` to replace a particular instance.

```scala mdoc:silent
implicit val diffPerson: Diff[Person] = Diff.summon[Person].modify(_.weight)
        .setTo(Diff.approximate(epsilon=5))
```

```scala mdoc
compare(Person(23, 60), Person(23, 62))
```

In fact, replacement is so powerful that ignoring is implemented as a replacement 
with the `Diff.ignore` instance.


## collection support

Specify how objects within particular collection within particular diff instance should be matched.
We distinguish three main types of collections:
- seqLike collections where elements are indexed collections
- setLike collections where elements aren't indexed
- mapLike collections where elements(values) are indexed by some keys

Each collection should fall into one of above categories. 
Each category exposes different set of methods.

```scala mdoc:silent
case class Organization(peopleList: List[Person], peopleSet: Set[Person], peopleMap: Map[Person, Person])
implicit val diffOrg: Diff[Organization] = Diff.summon[Organization]
        // seqLike methods:
        .modify(_.peopleList).matchByValue(_.age)
        .modify(_.peopleList).matchByIndex(index => index % 2)
        // setLike methods:
        .modify(_.peopleSet).matchBy(_.age)
        // mapLike methods:
        .modify(_.peopleMap).matchByValue(_.age)
        .modify(_.peopleMap).matchByKey(_.weight)
```
