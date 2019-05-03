package asserts.diff

trait DiffFor[T] {

  def diff(left: T, right: T): DiffResult
}

object DiffFor extends LowPriorityDiffForInstances {
  def apply[T, R: DiffFor](converter: T => R): DiffFor[T] =
    (left: T, right: T) => implicitly[DiffFor[R]].diff(converter(left), converter(right))
}

trait LowPriorityDiffForInstances {

  implicit def diffForAny[T <: AnyVal]: DiffFor[T] = (left: T, right: T) => {
    if (left.toString != right.toString) {
      DiffResultValue(left, right)
    } else {
      Identical(left)
    }
  }

  implicit def diffForString: DiffFor[String] = (left: String, right: String) => {
    if (left != right) {
      DiffResultValue(left, right)
    } else {
      Identical(left)
    }
  }
}