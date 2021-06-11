package com.softwaremill.diffx

trait TupleInstances {

  implicit def dTuple2[T1, T2](implicit d1: Diff[T1], d2: Diff[T2]): Diff[Tuple2[T1, T2]] = new Diff[Tuple2[T1, T2]] {
    override def apply(
        left: (T1, T2),
        right: (T1, T2),
        toIgnore: List[_root_.com.softwaremill.diffx.FieldPath]
    ): DiffResult = {
      val results = List("_1" -> d1.apply(left._1, right._1), "_2" -> d2.apply(left._2, right._2)).toMap
      if (results.values.forall(_.isIdentical)) {
        Identical(left)
      } else {
        DiffResultObject("Tuple2", results)
      }
    }
  }

  implicit def dTuple3[T1, T2, T3](implicit d1: Diff[T1], d2: Diff[T2], d3: Diff[T3]): Diff[Tuple3[T1, T2, T3]] =
    new Diff[Tuple3[T1, T2, T3]] {
      override def apply(
          left: (T1, T2, T3),
          right: (T1, T2, T3),
          toIgnore: List[_root_.com.softwaremill.diffx.FieldPath]
      ): DiffResult = {
        val results = List(
          "_1" -> d1.apply(left._1, right._1),
          "_2" -> d2.apply(left._2, right._2),
          "_3" -> d3.apply(left._3, right._3)
        ).toMap
        if (results.values.forall(_.isIdentical)) {
          Identical(left)
        } else {
          DiffResultObject("Tuple3", results)
        }
      }
    }

  implicit def dTuple4[T1, T2, T3, T4](implicit
      d1: Diff[T1],
      d2: Diff[T2],
      d3: Diff[T3],
      d4: Diff[T4]
  ): Diff[Tuple4[T1, T2, T3, T4]] = new Diff[Tuple4[T1, T2, T3, T4]] {
    override def apply(
        left: (T1, T2, T3, T4),
        right: (T1, T2, T3, T4),
        toIgnore: List[_root_.com.softwaremill.diffx.FieldPath]
    ): DiffResult = {
      val results = List(
        "_1" -> d1.apply(left._1, right._1),
        "_2" -> d2.apply(left._2, right._2),
        "_3" -> d3.apply(left._3, right._3),
        "_4" -> d4.apply(left._4, right._4)
      ).toMap
      if (results.values.forall(_.isIdentical)) {
        Identical(left)
      } else {
        DiffResultObject("Tuple4", results)
      }
    }
  }

  implicit def dTuple5[T1, T2, T3, T4, T5](implicit
      d1: Diff[T1],
      d2: Diff[T2],
      d3: Diff[T3],
      d4: Diff[T4],
      d5: Diff[T5]
  ): Diff[Tuple5[T1, T2, T3, T4, T5]] = new Diff[Tuple5[T1, T2, T3, T4, T5]] {
    override def apply(
        left: (T1, T2, T3, T4, T5),
        right: (T1, T2, T3, T4, T5),
        toIgnore: List[_root_.com.softwaremill.diffx.FieldPath]
    ): DiffResult = {
      val results = List(
        "_1" -> d1.apply(left._1, right._1),
        "_2" -> d2.apply(left._2, right._2),
        "_3" -> d3.apply(left._3, right._3),
        "_4" -> d4.apply(left._4, right._4),
        "_5" -> d5.apply(left._5, right._5)
      ).toMap
      if (results.values.forall(_.isIdentical)) {
        Identical(left)
      } else {
        DiffResultObject("Tuple5", results)
      }
    }
  }

  implicit def dTuple6[T1, T2, T3, T4, T5, T6](implicit
      d1: Diff[T1],
      d2: Diff[T2],
      d3: Diff[T3],
      d4: Diff[T4],
      d5: Diff[T5],
      d6: Diff[T6]
  ): Diff[Tuple6[T1, T2, T3, T4, T5, T6]] = new Diff[Tuple6[T1, T2, T3, T4, T5, T6]] {
    override def apply(
        left: (T1, T2, T3, T4, T5, T6),
        right: (T1, T2, T3, T4, T5, T6),
        toIgnore: List[_root_.com.softwaremill.diffx.FieldPath]
    ): DiffResult = {
      val results = List(
        "_1" -> d1.apply(left._1, right._1),
        "_2" -> d2.apply(left._2, right._2),
        "_3" -> d3.apply(left._3, right._3),
        "_4" -> d4.apply(left._4, right._4),
        "_5" -> d5.apply(left._5, right._5),
        "_6" -> d6.apply(left._6, right._6)
      ).toMap
      if (results.values.forall(_.isIdentical)) {
        Identical(left)
      } else {
        DiffResultObject("Tuple6", results)
      }
    }
  }

  implicit def dTuple7[T1, T2, T3, T4, T5, T6, T7](implicit
      d1: Diff[T1],
      d2: Diff[T2],
      d3: Diff[T3],
      d4: Diff[T4],
      d5: Diff[T5],
      d6: Diff[T6],
      d7: Diff[T7]
  ): Diff[Tuple7[T1, T2, T3, T4, T5, T6, T7]] = new Diff[Tuple7[T1, T2, T3, T4, T5, T6, T7]] {
    override def apply(
        left: (T1, T2, T3, T4, T5, T6, T7),
        right: (T1, T2, T3, T4, T5, T6, T7),
        toIgnore: List[_root_.com.softwaremill.diffx.FieldPath]
    ): DiffResult = {
      val results = List(
        "_1" -> d1.apply(left._1, right._1),
        "_2" -> d2.apply(left._2, right._2),
        "_3" -> d3.apply(left._3, right._3),
        "_4" -> d4.apply(left._4, right._4),
        "_5" -> d5.apply(left._5, right._5),
        "_6" -> d6.apply(left._6, right._6),
        "_7" -> d7.apply(left._7, right._7)
      ).toMap
      if (results.values.forall(_.isIdentical)) {
        Identical(left)
      } else {
        DiffResultObject("Tuple7", results)
      }
    }
  }

  implicit def dTuple8[T1, T2, T3, T4, T5, T6, T7, T8](implicit
      d1: Diff[T1],
      d2: Diff[T2],
      d3: Diff[T3],
      d4: Diff[T4],
      d5: Diff[T5],
      d6: Diff[T6],
      d7: Diff[T7],
      d8: Diff[T8]
  ): Diff[Tuple8[T1, T2, T3, T4, T5, T6, T7, T8]] = new Diff[Tuple8[T1, T2, T3, T4, T5, T6, T7, T8]] {
    override def apply(
        left: (T1, T2, T3, T4, T5, T6, T7, T8),
        right: (T1, T2, T3, T4, T5, T6, T7, T8),
        toIgnore: List[_root_.com.softwaremill.diffx.FieldPath]
    ): DiffResult = {
      val results = List(
        "_1" -> d1.apply(left._1, right._1),
        "_2" -> d2.apply(left._2, right._2),
        "_3" -> d3.apply(left._3, right._3),
        "_4" -> d4.apply(left._4, right._4),
        "_5" -> d5.apply(left._5, right._5),
        "_6" -> d6.apply(left._6, right._6),
        "_7" -> d7.apply(left._7, right._7),
        "_8" -> d8.apply(left._8, right._8)
      ).toMap
      if (results.values.forall(_.isIdentical)) {
        Identical(left)
      } else {
        DiffResultObject("Tuple8", results)
      }
    }
  }

  implicit def dTuple9[T1, T2, T3, T4, T5, T6, T7, T8, T9](implicit
      d1: Diff[T1],
      d2: Diff[T2],
      d3: Diff[T3],
      d4: Diff[T4],
      d5: Diff[T5],
      d6: Diff[T6],
      d7: Diff[T7],
      d8: Diff[T8],
      d9: Diff[T9]
  ): Diff[Tuple9[T1, T2, T3, T4, T5, T6, T7, T8, T9]] = new Diff[Tuple9[T1, T2, T3, T4, T5, T6, T7, T8, T9]] {
    override def apply(
        left: (T1, T2, T3, T4, T5, T6, T7, T8, T9),
        right: (T1, T2, T3, T4, T5, T6, T7, T8, T9),
        toIgnore: List[_root_.com.softwaremill.diffx.FieldPath]
    ): DiffResult = {
      val results = List(
        "_1" -> d1.apply(left._1, right._1),
        "_2" -> d2.apply(left._2, right._2),
        "_3" -> d3.apply(left._3, right._3),
        "_4" -> d4.apply(left._4, right._4),
        "_5" -> d5.apply(left._5, right._5),
        "_6" -> d6.apply(left._6, right._6),
        "_7" -> d7.apply(left._7, right._7),
        "_8" -> d8.apply(left._8, right._8),
        "_9" -> d9.apply(left._9, right._9)
      ).toMap
      if (results.values.forall(_.isIdentical)) {
        Identical(left)
      } else {
        DiffResultObject("Tuple9", results)
      }
    }
  }

  implicit def dTuple10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10](implicit
      d1: Diff[T1],
      d2: Diff[T2],
      d3: Diff[T3],
      d4: Diff[T4],
      d5: Diff[T5],
      d6: Diff[T6],
      d7: Diff[T7],
      d8: Diff[T8],
      d9: Diff[T9],
      d10: Diff[T10]
  ): Diff[Tuple10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10]] =
    new Diff[Tuple10[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10]] {
      override def apply(
          left: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10),
          right: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10),
          toIgnore: List[_root_.com.softwaremill.diffx.FieldPath]
      ): DiffResult = {
        val results = List(
          "_1" -> d1.apply(left._1, right._1),
          "_2" -> d2.apply(left._2, right._2),
          "_3" -> d3.apply(left._3, right._3),
          "_4" -> d4.apply(left._4, right._4),
          "_5" -> d5.apply(left._5, right._5),
          "_6" -> d6.apply(left._6, right._6),
          "_7" -> d7.apply(left._7, right._7),
          "_8" -> d8.apply(left._8, right._8),
          "_9" -> d9.apply(left._9, right._9),
          "_10" -> d10.apply(left._10, right._10)
        ).toMap
        if (results.values.forall(_.isIdentical)) {
          Identical(left)
        } else {
          DiffResultObject("Tuple10", results)
        }
      }
    }

  implicit def dTuple11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11](implicit
      d1: Diff[T1],
      d2: Diff[T2],
      d3: Diff[T3],
      d4: Diff[T4],
      d5: Diff[T5],
      d6: Diff[T6],
      d7: Diff[T7],
      d8: Diff[T8],
      d9: Diff[T9],
      d10: Diff[T10],
      d11: Diff[T11]
  ): Diff[Tuple11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11]] =
    new Diff[Tuple11[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11]] {
      override def apply(
          left: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11),
          right: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11),
          toIgnore: List[_root_.com.softwaremill.diffx.FieldPath]
      ): DiffResult = {
        val results = List(
          "_1" -> d1.apply(left._1, right._1),
          "_2" -> d2.apply(left._2, right._2),
          "_3" -> d3.apply(left._3, right._3),
          "_4" -> d4.apply(left._4, right._4),
          "_5" -> d5.apply(left._5, right._5),
          "_6" -> d6.apply(left._6, right._6),
          "_7" -> d7.apply(left._7, right._7),
          "_8" -> d8.apply(left._8, right._8),
          "_9" -> d9.apply(left._9, right._9),
          "_10" -> d10.apply(left._10, right._10),
          "_11" -> d11.apply(left._11, right._11)
        ).toMap
        if (results.values.forall(_.isIdentical)) {
          Identical(left)
        } else {
          DiffResultObject("Tuple11", results)
        }
      }
    }

  implicit def dTuple12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12](implicit
      d1: Diff[T1],
      d2: Diff[T2],
      d3: Diff[T3],
      d4: Diff[T4],
      d5: Diff[T5],
      d6: Diff[T6],
      d7: Diff[T7],
      d8: Diff[T8],
      d9: Diff[T9],
      d10: Diff[T10],
      d11: Diff[T11],
      d12: Diff[T12]
  ): Diff[Tuple12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12]] =
    new Diff[Tuple12[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12]] {
      override def apply(
          left: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12),
          right: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12),
          toIgnore: List[_root_.com.softwaremill.diffx.FieldPath]
      ): DiffResult = {
        val results = List(
          "_1" -> d1.apply(left._1, right._1),
          "_2" -> d2.apply(left._2, right._2),
          "_3" -> d3.apply(left._3, right._3),
          "_4" -> d4.apply(left._4, right._4),
          "_5" -> d5.apply(left._5, right._5),
          "_6" -> d6.apply(left._6, right._6),
          "_7" -> d7.apply(left._7, right._7),
          "_8" -> d8.apply(left._8, right._8),
          "_9" -> d9.apply(left._9, right._9),
          "_10" -> d10.apply(left._10, right._10),
          "_11" -> d11.apply(left._11, right._11),
          "_12" -> d12.apply(left._12, right._12)
        ).toMap
        if (results.values.forall(_.isIdentical)) {
          Identical(left)
        } else {
          DiffResultObject("Tuple12", results)
        }
      }
    }

  implicit def dTuple13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13](implicit
      d1: Diff[T1],
      d2: Diff[T2],
      d3: Diff[T3],
      d4: Diff[T4],
      d5: Diff[T5],
      d6: Diff[T6],
      d7: Diff[T7],
      d8: Diff[T8],
      d9: Diff[T9],
      d10: Diff[T10],
      d11: Diff[T11],
      d12: Diff[T12],
      d13: Diff[T13]
  ): Diff[Tuple13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13]] =
    new Diff[Tuple13[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13]] {
      override def apply(
          left: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13),
          right: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13),
          toIgnore: List[_root_.com.softwaremill.diffx.FieldPath]
      ): DiffResult = {
        val results = List(
          "_1" -> d1.apply(left._1, right._1),
          "_2" -> d2.apply(left._2, right._2),
          "_3" -> d3.apply(left._3, right._3),
          "_4" -> d4.apply(left._4, right._4),
          "_5" -> d5.apply(left._5, right._5),
          "_6" -> d6.apply(left._6, right._6),
          "_7" -> d7.apply(left._7, right._7),
          "_8" -> d8.apply(left._8, right._8),
          "_9" -> d9.apply(left._9, right._9),
          "_10" -> d10.apply(left._10, right._10),
          "_11" -> d11.apply(left._11, right._11),
          "_12" -> d12.apply(left._12, right._12),
          "_13" -> d13.apply(left._13, right._13)
        ).toMap
        if (results.values.forall(_.isIdentical)) {
          Identical(left)
        } else {
          DiffResultObject("Tuple13", results)
        }
      }
    }

  implicit def dTuple14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14](implicit
      d1: Diff[T1],
      d2: Diff[T2],
      d3: Diff[T3],
      d4: Diff[T4],
      d5: Diff[T5],
      d6: Diff[T6],
      d7: Diff[T7],
      d8: Diff[T8],
      d9: Diff[T9],
      d10: Diff[T10],
      d11: Diff[T11],
      d12: Diff[T12],
      d13: Diff[T13],
      d14: Diff[T14]
  ): Diff[Tuple14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14]] =
    new Diff[Tuple14[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14]] {
      override def apply(
          left: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14),
          right: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14),
          toIgnore: List[_root_.com.softwaremill.diffx.FieldPath]
      ): DiffResult = {
        val results = List(
          "_1" -> d1.apply(left._1, right._1),
          "_2" -> d2.apply(left._2, right._2),
          "_3" -> d3.apply(left._3, right._3),
          "_4" -> d4.apply(left._4, right._4),
          "_5" -> d5.apply(left._5, right._5),
          "_6" -> d6.apply(left._6, right._6),
          "_7" -> d7.apply(left._7, right._7),
          "_8" -> d8.apply(left._8, right._8),
          "_9" -> d9.apply(left._9, right._9),
          "_10" -> d10.apply(left._10, right._10),
          "_11" -> d11.apply(left._11, right._11),
          "_12" -> d12.apply(left._12, right._12),
          "_13" -> d13.apply(left._13, right._13),
          "_14" -> d14.apply(left._14, right._14)
        ).toMap
        if (results.values.forall(_.isIdentical)) {
          Identical(left)
        } else {
          DiffResultObject("Tuple14", results)
        }
      }
    }

  implicit def dTuple15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15](implicit
      d1: Diff[T1],
      d2: Diff[T2],
      d3: Diff[T3],
      d4: Diff[T4],
      d5: Diff[T5],
      d6: Diff[T6],
      d7: Diff[T7],
      d8: Diff[T8],
      d9: Diff[T9],
      d10: Diff[T10],
      d11: Diff[T11],
      d12: Diff[T12],
      d13: Diff[T13],
      d14: Diff[T14],
      d15: Diff[T15]
  ): Diff[Tuple15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15]] =
    new Diff[Tuple15[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15]] {
      override def apply(
          left: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15),
          right: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15),
          toIgnore: List[_root_.com.softwaremill.diffx.FieldPath]
      ): DiffResult = {
        val results = List(
          "_1" -> d1.apply(left._1, right._1),
          "_2" -> d2.apply(left._2, right._2),
          "_3" -> d3.apply(left._3, right._3),
          "_4" -> d4.apply(left._4, right._4),
          "_5" -> d5.apply(left._5, right._5),
          "_6" -> d6.apply(left._6, right._6),
          "_7" -> d7.apply(left._7, right._7),
          "_8" -> d8.apply(left._8, right._8),
          "_9" -> d9.apply(left._9, right._9),
          "_10" -> d10.apply(left._10, right._10),
          "_11" -> d11.apply(left._11, right._11),
          "_12" -> d12.apply(left._12, right._12),
          "_13" -> d13.apply(left._13, right._13),
          "_14" -> d14.apply(left._14, right._14),
          "_15" -> d15.apply(left._15, right._15)
        ).toMap
        if (results.values.forall(_.isIdentical)) {
          Identical(left)
        } else {
          DiffResultObject("Tuple15", results)
        }
      }
    }

  implicit def dTuple16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16](implicit
      d1: Diff[T1],
      d2: Diff[T2],
      d3: Diff[T3],
      d4: Diff[T4],
      d5: Diff[T5],
      d6: Diff[T6],
      d7: Diff[T7],
      d8: Diff[T8],
      d9: Diff[T9],
      d10: Diff[T10],
      d11: Diff[T11],
      d12: Diff[T12],
      d13: Diff[T13],
      d14: Diff[T14],
      d15: Diff[T15],
      d16: Diff[T16]
  ): Diff[Tuple16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16]] =
    new Diff[Tuple16[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16]] {
      override def apply(
          left: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16),
          right: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16),
          toIgnore: List[_root_.com.softwaremill.diffx.FieldPath]
      ): DiffResult = {
        val results = List(
          "_1" -> d1.apply(left._1, right._1),
          "_2" -> d2.apply(left._2, right._2),
          "_3" -> d3.apply(left._3, right._3),
          "_4" -> d4.apply(left._4, right._4),
          "_5" -> d5.apply(left._5, right._5),
          "_6" -> d6.apply(left._6, right._6),
          "_7" -> d7.apply(left._7, right._7),
          "_8" -> d8.apply(left._8, right._8),
          "_9" -> d9.apply(left._9, right._9),
          "_10" -> d10.apply(left._10, right._10),
          "_11" -> d11.apply(left._11, right._11),
          "_12" -> d12.apply(left._12, right._12),
          "_13" -> d13.apply(left._13, right._13),
          "_14" -> d14.apply(left._14, right._14),
          "_15" -> d15.apply(left._15, right._15),
          "_16" -> d16.apply(left._16, right._16)
        ).toMap
        if (results.values.forall(_.isIdentical)) {
          Identical(left)
        } else {
          DiffResultObject("Tuple16", results)
        }
      }
    }

  implicit def dTuple17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17](implicit
      d1: Diff[T1],
      d2: Diff[T2],
      d3: Diff[T3],
      d4: Diff[T4],
      d5: Diff[T5],
      d6: Diff[T6],
      d7: Diff[T7],
      d8: Diff[T8],
      d9: Diff[T9],
      d10: Diff[T10],
      d11: Diff[T11],
      d12: Diff[T12],
      d13: Diff[T13],
      d14: Diff[T14],
      d15: Diff[T15],
      d16: Diff[T16],
      d17: Diff[T17]
  ): Diff[Tuple17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17]] =
    new Diff[Tuple17[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17]] {
      override def apply(
          left: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17),
          right: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17),
          toIgnore: List[_root_.com.softwaremill.diffx.FieldPath]
      ): DiffResult = {
        val results = List(
          "_1" -> d1.apply(left._1, right._1),
          "_2" -> d2.apply(left._2, right._2),
          "_3" -> d3.apply(left._3, right._3),
          "_4" -> d4.apply(left._4, right._4),
          "_5" -> d5.apply(left._5, right._5),
          "_6" -> d6.apply(left._6, right._6),
          "_7" -> d7.apply(left._7, right._7),
          "_8" -> d8.apply(left._8, right._8),
          "_9" -> d9.apply(left._9, right._9),
          "_10" -> d10.apply(left._10, right._10),
          "_11" -> d11.apply(left._11, right._11),
          "_12" -> d12.apply(left._12, right._12),
          "_13" -> d13.apply(left._13, right._13),
          "_14" -> d14.apply(left._14, right._14),
          "_15" -> d15.apply(left._15, right._15),
          "_16" -> d16.apply(left._16, right._16),
          "_17" -> d17.apply(left._17, right._17)
        ).toMap
        if (results.values.forall(_.isIdentical)) {
          Identical(left)
        } else {
          DiffResultObject("Tuple17", results)
        }
      }
    }

  implicit def dTuple18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18](implicit
      d1: Diff[T1],
      d2: Diff[T2],
      d3: Diff[T3],
      d4: Diff[T4],
      d5: Diff[T5],
      d6: Diff[T6],
      d7: Diff[T7],
      d8: Diff[T8],
      d9: Diff[T9],
      d10: Diff[T10],
      d11: Diff[T11],
      d12: Diff[T12],
      d13: Diff[T13],
      d14: Diff[T14],
      d15: Diff[T15],
      d16: Diff[T16],
      d17: Diff[T17],
      d18: Diff[T18]
  ): Diff[Tuple18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18]] =
    new Diff[Tuple18[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18]] {
      override def apply(
          left: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18),
          right: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18),
          toIgnore: List[_root_.com.softwaremill.diffx.FieldPath]
      ): DiffResult = {
        val results = List(
          "_1" -> d1.apply(left._1, right._1),
          "_2" -> d2.apply(left._2, right._2),
          "_3" -> d3.apply(left._3, right._3),
          "_4" -> d4.apply(left._4, right._4),
          "_5" -> d5.apply(left._5, right._5),
          "_6" -> d6.apply(left._6, right._6),
          "_7" -> d7.apply(left._7, right._7),
          "_8" -> d8.apply(left._8, right._8),
          "_9" -> d9.apply(left._9, right._9),
          "_10" -> d10.apply(left._10, right._10),
          "_11" -> d11.apply(left._11, right._11),
          "_12" -> d12.apply(left._12, right._12),
          "_13" -> d13.apply(left._13, right._13),
          "_14" -> d14.apply(left._14, right._14),
          "_15" -> d15.apply(left._15, right._15),
          "_16" -> d16.apply(left._16, right._16),
          "_17" -> d17.apply(left._17, right._17),
          "_18" -> d18.apply(left._18, right._18)
        ).toMap
        if (results.values.forall(_.isIdentical)) {
          Identical(left)
        } else {
          DiffResultObject("Tuple18", results)
        }
      }
    }

  implicit def dTuple19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19](implicit
      d1: Diff[T1],
      d2: Diff[T2],
      d3: Diff[T3],
      d4: Diff[T4],
      d5: Diff[T5],
      d6: Diff[T6],
      d7: Diff[T7],
      d8: Diff[T8],
      d9: Diff[T9],
      d10: Diff[T10],
      d11: Diff[T11],
      d12: Diff[T12],
      d13: Diff[T13],
      d14: Diff[T14],
      d15: Diff[T15],
      d16: Diff[T16],
      d17: Diff[T17],
      d18: Diff[T18],
      d19: Diff[T19]
  ): Diff[Tuple19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19]] =
    new Diff[Tuple19[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19]] {
      override def apply(
          left: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19),
          right: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19),
          toIgnore: List[_root_.com.softwaremill.diffx.FieldPath]
      ): DiffResult = {
        val results = List(
          "_1" -> d1.apply(left._1, right._1),
          "_2" -> d2.apply(left._2, right._2),
          "_3" -> d3.apply(left._3, right._3),
          "_4" -> d4.apply(left._4, right._4),
          "_5" -> d5.apply(left._5, right._5),
          "_6" -> d6.apply(left._6, right._6),
          "_7" -> d7.apply(left._7, right._7),
          "_8" -> d8.apply(left._8, right._8),
          "_9" -> d9.apply(left._9, right._9),
          "_10" -> d10.apply(left._10, right._10),
          "_11" -> d11.apply(left._11, right._11),
          "_12" -> d12.apply(left._12, right._12),
          "_13" -> d13.apply(left._13, right._13),
          "_14" -> d14.apply(left._14, right._14),
          "_15" -> d15.apply(left._15, right._15),
          "_16" -> d16.apply(left._16, right._16),
          "_17" -> d17.apply(left._17, right._17),
          "_18" -> d18.apply(left._18, right._18),
          "_19" -> d19.apply(left._19, right._19)
        ).toMap
        if (results.values.forall(_.isIdentical)) {
          Identical(left)
        } else {
          DiffResultObject("Tuple19", results)
        }
      }
    }

  implicit def dTuple20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20](
      implicit
      d1: Diff[T1],
      d2: Diff[T2],
      d3: Diff[T3],
      d4: Diff[T4],
      d5: Diff[T5],
      d6: Diff[T6],
      d7: Diff[T7],
      d8: Diff[T8],
      d9: Diff[T9],
      d10: Diff[T10],
      d11: Diff[T11],
      d12: Diff[T12],
      d13: Diff[T13],
      d14: Diff[T14],
      d15: Diff[T15],
      d16: Diff[T16],
      d17: Diff[T17],
      d18: Diff[T18],
      d19: Diff[T19],
      d20: Diff[T20]
  ): Diff[Tuple20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20]] =
    new Diff[Tuple20[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20]] {
      override def apply(
          left: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20),
          right: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20),
          toIgnore: List[_root_.com.softwaremill.diffx.FieldPath]
      ): DiffResult = {
        val results = List(
          "_1" -> d1.apply(left._1, right._1),
          "_2" -> d2.apply(left._2, right._2),
          "_3" -> d3.apply(left._3, right._3),
          "_4" -> d4.apply(left._4, right._4),
          "_5" -> d5.apply(left._5, right._5),
          "_6" -> d6.apply(left._6, right._6),
          "_7" -> d7.apply(left._7, right._7),
          "_8" -> d8.apply(left._8, right._8),
          "_9" -> d9.apply(left._9, right._9),
          "_10" -> d10.apply(left._10, right._10),
          "_11" -> d11.apply(left._11, right._11),
          "_12" -> d12.apply(left._12, right._12),
          "_13" -> d13.apply(left._13, right._13),
          "_14" -> d14.apply(left._14, right._14),
          "_15" -> d15.apply(left._15, right._15),
          "_16" -> d16.apply(left._16, right._16),
          "_17" -> d17.apply(left._17, right._17),
          "_18" -> d18.apply(left._18, right._18),
          "_19" -> d19.apply(left._19, right._19),
          "_20" -> d20.apply(left._20, right._20)
        ).toMap
        if (results.values.forall(_.isIdentical)) {
          Identical(left)
        } else {
          DiffResultObject("Tuple20", results)
        }
      }
    }

  implicit def dTuple21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21](
      implicit
      d1: Diff[T1],
      d2: Diff[T2],
      d3: Diff[T3],
      d4: Diff[T4],
      d5: Diff[T5],
      d6: Diff[T6],
      d7: Diff[T7],
      d8: Diff[T8],
      d9: Diff[T9],
      d10: Diff[T10],
      d11: Diff[T11],
      d12: Diff[T12],
      d13: Diff[T13],
      d14: Diff[T14],
      d15: Diff[T15],
      d16: Diff[T16],
      d17: Diff[T17],
      d18: Diff[T18],
      d19: Diff[T19],
      d20: Diff[T20],
      d21: Diff[T21]
  ): Diff[Tuple21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21]] =
    new Diff[Tuple21[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21]] {
      override def apply(
          left: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21),
          right: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21),
          toIgnore: List[_root_.com.softwaremill.diffx.FieldPath]
      ): DiffResult = {
        val results = List(
          "_1" -> d1.apply(left._1, right._1),
          "_2" -> d2.apply(left._2, right._2),
          "_3" -> d3.apply(left._3, right._3),
          "_4" -> d4.apply(left._4, right._4),
          "_5" -> d5.apply(left._5, right._5),
          "_6" -> d6.apply(left._6, right._6),
          "_7" -> d7.apply(left._7, right._7),
          "_8" -> d8.apply(left._8, right._8),
          "_9" -> d9.apply(left._9, right._9),
          "_10" -> d10.apply(left._10, right._10),
          "_11" -> d11.apply(left._11, right._11),
          "_12" -> d12.apply(left._12, right._12),
          "_13" -> d13.apply(left._13, right._13),
          "_14" -> d14.apply(left._14, right._14),
          "_15" -> d15.apply(left._15, right._15),
          "_16" -> d16.apply(left._16, right._16),
          "_17" -> d17.apply(left._17, right._17),
          "_18" -> d18.apply(left._18, right._18),
          "_19" -> d19.apply(left._19, right._19),
          "_20" -> d20.apply(left._20, right._20),
          "_21" -> d21.apply(left._21, right._21)
        ).toMap
        if (results.values.forall(_.isIdentical)) {
          Identical(left)
        } else {
          DiffResultObject("Tuple21", results)
        }
      }
    }

  implicit def dTuple22[
      T1,
      T2,
      T3,
      T4,
      T5,
      T6,
      T7,
      T8,
      T9,
      T10,
      T11,
      T12,
      T13,
      T14,
      T15,
      T16,
      T17,
      T18,
      T19,
      T20,
      T21,
      T22
  ](implicit
      d1: Diff[T1],
      d2: Diff[T2],
      d3: Diff[T3],
      d4: Diff[T4],
      d5: Diff[T5],
      d6: Diff[T6],
      d7: Diff[T7],
      d8: Diff[T8],
      d9: Diff[T9],
      d10: Diff[T10],
      d11: Diff[T11],
      d12: Diff[T12],
      d13: Diff[T13],
      d14: Diff[T14],
      d15: Diff[T15],
      d16: Diff[T16],
      d17: Diff[T17],
      d18: Diff[T18],
      d19: Diff[T19],
      d20: Diff[T20],
      d21: Diff[T21],
      d22: Diff[T22]
  ): Diff[
    Tuple22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22]
  ] = new Diff[
    Tuple22[T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22]
  ] {
    override def apply(
        left: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22),
        right: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22),
        toIgnore: List[_root_.com.softwaremill.diffx.FieldPath]
    ): DiffResult = {
      val results = List(
        "_1" -> d1.apply(left._1, right._1),
        "_2" -> d2.apply(left._2, right._2),
        "_3" -> d3.apply(left._3, right._3),
        "_4" -> d4.apply(left._4, right._4),
        "_5" -> d5.apply(left._5, right._5),
        "_6" -> d6.apply(left._6, right._6),
        "_7" -> d7.apply(left._7, right._7),
        "_8" -> d8.apply(left._8, right._8),
        "_9" -> d9.apply(left._9, right._9),
        "_10" -> d10.apply(left._10, right._10),
        "_11" -> d11.apply(left._11, right._11),
        "_12" -> d12.apply(left._12, right._12),
        "_13" -> d13.apply(left._13, right._13),
        "_14" -> d14.apply(left._14, right._14),
        "_15" -> d15.apply(left._15, right._15),
        "_16" -> d16.apply(left._16, right._16),
        "_17" -> d17.apply(left._17, right._17),
        "_18" -> d18.apply(left._18, right._18),
        "_19" -> d19.apply(left._19, right._19),
        "_20" -> d20.apply(left._20, right._20),
        "_21" -> d21.apply(left._21, right._21),
        "_22" -> d22.apply(left._22, right._22)
      ).toMap
      if (results.values.forall(_.isIdentical)) {
        Identical(left)
      } else {
        DiffResultObject("Tuple22", results)
      }
    }
  }

}
