# replacing

Sometimes you might want to compare some nested values using a different comparator but, 
the type they share is not unique within that hierarchy.

Consider following example:
```scala mdoc
class Person(age: Int, weight: Int)
```

If we would like to compare `weight` differently than `age` we would have to introduce a new type for `weight` 
in order to provide a different `Diff` typeclass for only that field. While in general, it is a good idea to have your types 
very precise it might not always be practical or even possible. Fortunately, diffx comes with a mechanism which allows
the replacement of nested diff instances:

```scala mdoc
implicit val diffPerson: Derived[Diff[Person]] = Diff.derived[Person].modify(_.weight)
        .setTo(Diff.approximate(epsilon=5))
compare(Person(23, 60), Person(23, 62))
```