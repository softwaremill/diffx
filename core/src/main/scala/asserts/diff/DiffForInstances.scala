package asserts.diff

import java.time.Instant

import scala.reflect.ClassTag

trait DiffForInstances {
  implicit val diffForString: DiffFor[String] = (left: String, right: String) => {
    if (left.toString != right.toString) {
      DiffResultValue(left, right)
    } else {
      Identical2(left)
    }
  }

  implicit val diffForInt: DiffFor[Int] = DiffFor.apply(_.toString)
  implicit val diffForBoolean: DiffFor[Boolean] = DiffFor.apply(_.toString)
  implicit val diffForLong: DiffFor[Long] = DiffFor.apply(_.toString)
  implicit val diffForFloat: DiffFor[Float] = DiffFor.apply(_.toString)
  implicit val diffForDouble: DiffFor[Double] = DiffFor.apply(_.toString)
  implicit val diffForInstant: DiffFor[Instant] = DiffFor.apply(_.toString)

  implicit def diffForOption[T: DiffFor]: DiffFor[Option[T]] = (left: Option[T], right: Option[T]) => {
    (left, right) match {
      case (Some(l), Some(r)) => implicitly[DiffFor[T]].diff(l, r)
      case (None, None)       => Identical2(None)
      case (l, r)             => DiffResultValue(l, r)
    }
  }

  implicit def diffForIterable[T: DiffFor, C[_] <: Iterable[_]]: DiffFor[C[T]] = new DiffFor[C[T]] {
    override def diff(left: C[T], right: C[T]): DiffResult = {
      val keySet = left.zipWithIndex.map(_._2) ++ right.zipWithIndex.map(_._2)
      DiffResultObject(
        "List",
        keySet.map { k =>
          k.toString -> implicitly[DiffFor[Option[T]]].diff(left.toList.asInstanceOf[List[T]].lift(k),
                                                            right.toList.asInstanceOf[List[T]].lift(k))
        }.toMap
      )
    }
  }

  implicit def diffForMap[T: DiffFor, C[_, _] <: Map[_, _]]: DiffFor[Map[String, T]] = new DiffFor[Map[String, T]] {
    override def diff(left: Map[String, T], right: Map[String, T]): DiffResult = {
      val keySet = left.keySet ++ right.keySet
      DiffResultObject("Map", keySet.map { k =>
        k -> implicitly[DiffFor[Option[T]]].diff(left.get(k), right.get(k))
      }.toMap)
    }
  }
}
