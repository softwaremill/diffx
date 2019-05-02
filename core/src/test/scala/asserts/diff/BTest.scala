package asserts.diff

import org.scalatest.{FlatSpec, Matchers}

class BTest extends FlatSpec with Matchers with DiffMatcher {

  val left: Foo = Foo(
    Bar("asdf", 5),
    List(123, 1234),
    Some(Bar("asdf", 5))
  )
  val right: Foo = Foo(
    Bar("asdf", 66),
    List(1234),
    Some(Bar("qwer", 5))
  )

  it should "work" in {
    import ai.x.diff.conversions._
    right should matchTo(right)
    left should matchTo(right)
  }
}