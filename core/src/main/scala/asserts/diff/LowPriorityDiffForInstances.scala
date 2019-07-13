package asserts.diff

trait LowPriorityDiffForInstances extends DiffForAnyDerivation {

  implicit def diffForString: DiffFor[String] = (left: String, right: String) => {
    if (left != right) {
      DiffResultValue(left, right)
    } else {
      Identical(left)
    }
  }
}
