# derivation

Diffx supports auto and semi-auto derivation.

For semi-auto derivation you don't need any additional import, just define your instances using:
```scala mdoc:compile-only
case class Product(name: String)
case class Basket(products: List[Product])

implicit val productDiff = Diff.derived[Product]
implicit val basketDiff = Diff.derived[Basket]
```

To use auto derivation add following import

`import com.softwaremill.diffx.generic.auto._`

or

extend trait

`com.softwaremill.diffx.generic.AutoDerivation`

**Auto derivation might have a huge impact on compilation times**, because of that it is recommended to use `semi-auto` derivation.
