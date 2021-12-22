package com.softwaremill.diffx

/** Defines how the elements within collections are paired
  *
  * @tparam T
  *   type of the collection element
  */
trait ObjectMatcher[T] {
  def isSameObject(left: T, right: T): Boolean
}

object ObjectMatcher extends LowPriorityObjectMatcher {
  def apply[T: ObjectMatcher]: ObjectMatcher[T] = implicitly[ObjectMatcher[T]]

  private def by[T, U: ObjectMatcher](f: T => U): ObjectMatcher[T] = (left: T, right: T) =>
    ObjectMatcher[U].isSameObject(f(left), f(right))

  /** Given MapEntry[K,V], match them using K's objectMatcher */
  implicit def mapEntryByKey[K: ObjectMatcher, V]: ObjectMatcher[MapEntry[K, V]] =
    ObjectMatcher.by[MapEntry[K, V], K](_.key)

  implicit def setEntryByValue[T: ObjectMatcher]: ObjectMatcher[SetEntry[T]] = ObjectMatcher.by[SetEntry[T], T](_.t)

  type SeqEntry[T] = MapEntry[Int, T]

  case class SetEntry[T](t: T)

  case class MapEntry[K, V](key: K, value: V)

  /** Matcher for all ordered collections e.g. [[List]], [[Seq]]. There has to exist an implicit instance of
    * [[com.softwaremill.diffx.SeqLike]] for such collection
    * @tparam T
    *   type of the element
    * @return
    */
  def seq[T] = new ObjectMatcherSeqHelper[T]

  class ObjectMatcherSeqHelper[V] {
    def byValue[U: ObjectMatcher](f: V => U): SeqMatcher[V] = byValue(ObjectMatcher.by[V, U](f))

    def byValue(implicit ev: ObjectMatcher[V]): SeqMatcher[V] =
      ObjectMatcher.by[SeqEntry[V], V](_.value)

    def byIndex[U: ObjectMatcher](f: Int => U): SeqMatcher[V] = byIndex(ObjectMatcher.by(f))

    def byIndex(implicit indexMatcher: ObjectMatcher[Int]): SeqMatcher[V] = ObjectMatcher.by(_.key)
  }

  /** Matcher for unordered collections like e.g. [[Set]]. There has to exist an implicit instance of
    * [[com.softwaremill.diffx.SetLike]] for such collection
    *
    * @tparam T
    *   type of the element
    * @return
    */
  def set[T] = new ObjectMatcherSetHelper[T]

  class ObjectMatcherSetHelper[T] {
    def by[U: ObjectMatcher](f: T => U): SetMatcher[T] = setEntryByValue(ObjectMatcher.by(f))
  }

  /** Matcher for map like collections e.g. [[Map]]. There has to exist an implicit instance of
    * [[com.softwaremill.diffx.MapLike]] for such collection
    * @tparam K
    *   type of the key
    * @tparam V
    *   type of the value
    * @return
    */
  def map[K, V] = new ObjectMatcherMapHelper[K, V]

  class ObjectMatcherMapHelper[K, V] {
    def byValue[U: ObjectMatcher](f: V => U): MapMatcher[K, V] = byValue(ObjectMatcher.by(f))

    def byValue(implicit ev: ObjectMatcher[V]): MapMatcher[K, V] = ObjectMatcher.by(_.value)

    def byKey[U: ObjectMatcher](f: K => U): MapMatcher[K, V] = byKey(ObjectMatcher.by(f))

    def byKey(implicit ko: ObjectMatcher[K]): MapMatcher[K, V] = ObjectMatcher.by(_.key)
  }
}

trait LowPriorityObjectMatcher {
  implicit def default[T]: ObjectMatcher[T] = (l: T, r: T) => l == r
}
