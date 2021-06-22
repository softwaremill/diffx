# extending

If you'd like to implement custom matching logic for the given type, create an implicit `Diff` instance for that
type, and make sure it's in scope when any `Diff` instances depending on that type are created.

Consider following example with `NonEmptyList` from cats. `NonEmptyList` is implemented as case class,
so the default behavior of diffx would be to create a `Diff[NonEmptyList]` typeclass instance using magnolia derivation.

Obviously that's not what we usually want. In most of the cases we would like `NonEmptyList` to be compared as a list.
Diffx already has an instance of a typeclass for a list (for any iterable to be precise). 
All we need to do is to use that typeclass by converting `NonEmptyList` to list which can be done using `contramap` method.

The final code looks as follows:

```scala
import cats.data.NonEmptyList
implicit def nelDiff[T: Diff]: Diff[NonEmptyList[T]] = 
    Diff[List[T]].contramap[NonEmptyList[T]](_.toList)
```

*Note: There is a diffx-cats module, so you don't have to do this*