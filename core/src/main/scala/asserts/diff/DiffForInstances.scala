package asserts.diff

import java.util.UUID

trait DiffForInstances extends LowPriorityDiffForInstances {

  implicit def diffForOption[T: DiffFor]: DiffFor[Option[T]] = (left: Option[T], right: Option[T]) => {
    (left, right) match {
      case (Some(l), Some(r)) => implicitly[DiffFor[T]].diff(l, r)
      case (None, None)       => Identical(None)
      case (l, r)             => DiffResultValue(l, r)
    }
  }

  implicit def diffForIterable[T: DiffFor, C[W] <: Iterable[W]]: DiffFor[C[T]] = (left: C[T], right: C[T]) => {
    val indexes = Range(0, Math.max(left.size, right.size))
    val leftAsMap = left.toList.lift
    val rightAsMap = right.toList.lift
    DiffResultObject(
      "List",
      indexes.map { index =>
        index.toString -> (implicitly[DiffFor[Option[T]]].diff(leftAsMap(index), rightAsMap(index)) match {
          case DiffResultValue(Some(v), None) => DiffResultAdditional(v)
          case DiffResultValue(None, Some(v)) => DiffResultMissing(v)
          case d                              => d
        })
      }.toMap
    )
  }

  implicit def diffForMap[T: DiffFor, C[_, _] <: Map[_, _]]: DiffFor[Map[String, T]] =
    (left: Map[String, T], right: Map[String, T]) => {
      val keySet = left.keySet ++ right.keySet
      DiffResultObject("Map", keySet.map { k =>
        k -> implicitly[DiffFor[Option[T]]].diff(left.get(k), right.get(k))
      }.toMap)
    }

  implicit val diffForBigDecimal: DiffFor[BigDecimal] = DiffFor.apply(_.toString)
  implicit val diffForUUID: DiffFor[UUID] = DiffFor.apply(_.toString)
}
