# derivation

Diffx supports auto and semi-auto derivation.

For semi-auto derivation you don't need any additional import, just define your instances using:
```scala mdoc:compile-only
import com.softwaremill.diffx._
case class Product(name: String)
case class Basket(products: List[Product])

implicit val productDiff = Diff.derived[Product]
implicit val basketDiff = Diff.derived[Basket]
```

To use auto derivation add following import

`import com.softwaremill.diffx.generic.auto._`

or extend trait

`com.softwaremill.diffx.generic.auto.AutoDerivation`

**Auto derivation might have a huge impact on compilation times**, because of that it is recommended to use `semi-auto` derivation.


Given that you have auto-derivation enabled you can summon diff instances as you would summon any other implicit type-class by using
`implictly[Diff[T]]`. You can also write a shorter version `Diff[T]` which will be equivalent.
However, if you would like to modify somehow (see [ignoring](./ignoring.md) and [replacing](./replacing.md)) given instance and 
put it back into to the implicit scope:
```scala 
implict val diffForMyClass: Diff[MyClass] = Diff[MyClass].doSomething
```
you would get a forward reference error. 

To overcome that issue there is a `Derived` wrapper which allows you to summon a wrapped instance.
```scala 
implict val diffForMyClass: Diff[MyClass] = Derived[Diff[MyClass]].value.doSomething
```
There is a `autoDerived` method to make it more convenient. Below code is equivalent to the one above.
```scala 
implict val diffForMyClass: Diff[MyClass] = Diff.autoDerived[MyClass].doSomething
```