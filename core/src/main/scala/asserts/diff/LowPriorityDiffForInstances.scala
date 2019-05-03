package asserts.diff

trait LowPriorityDiffForInstances {

  implicit def diffForAny[T <: AnyVal]: DiffFor[T] = (left: T, right: T) => {
    if (left.toString != right.toString) {
      DiffResultValue(left, right)
    } else {
      Identical2(left)
    }
  }

  implicit def diffForString: DiffFor[String] = (left: String, right: String) => {
    if (left != right) {
      DiffResultValue(left, right)
    } else {
      Identical2(left)
    }
  }
}
