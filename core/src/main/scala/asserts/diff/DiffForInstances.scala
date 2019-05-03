package asserts.diff

import java.time.Instant
import java.util.UUID

trait DiffForInstances extends DiffForDerivation {

  implicit def diffForOption[T: DiffFor]: DiffFor[Option[T]] = (left: Option[T], right: Option[T]) => {
    (left, right) match {
      case (Some(l), Some(r)) => implicitly[DiffFor[T]].diff(l, r)
      case (None, None)       => Identical2(None)
      case (l, r)             => DiffResultValue(l, r)
    }
  }

  implicit def diffForIterable[T: DiffFor, C[_] <: Iterable[_]]: DiffFor[C[T]] = (left: C[T], right: C[T]) => {
    val keySet = Range(0, Math.max(left.size, right.size))
    DiffResultObject(
      "List",
      keySet.map { k =>
        val leftValue = left.toList.asInstanceOf[List[T]].lift(k)
        val rightValue = right.toList.asInstanceOf[List[T]].lift(k)
        k.toString -> (implicitly[DiffFor[Option[T]]].diff(leftValue, rightValue) match {
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
  implicit val diffForInstant: DiffFor[Instant] = DiffFor.apply(_.toString)
  implicit val diffForUUID: DiffFor[UUID] = DiffFor.apply(_.toString)
}
