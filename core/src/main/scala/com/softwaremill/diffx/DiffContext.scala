package com.softwaremill.diffx

case class DiffContext(
    overrides: Tree[Diff[Any] => Diff[Any]],
    path: FieldPath,
    matcherOverrides: Tree[ObjectMatcher[_]]
) {
  def merge(other: DiffContext): DiffContext = {
    DiffContext(overrides.merge(other.overrides), List.empty, matcherOverrides.merge(other.matcherOverrides))
  }

  def getOverride(label: String): Option[Diff[Any] => Diff[Any]] = {
    treeOverride(label, overrides)
  }

  def getMatcherOverride[T]: Option[ObjectMatcher[T]] = {
    matcherOverrides match {
      case Tree.Leaf(v) => Some(v.asInstanceOf[ObjectMatcher[T]])
      case Tree.Node(_) => None
    }
  }

  private def treeOverride[T](label: String, tree: Tree[T]) = {
    tree match {
      case Tree.Leaf(_)     => throw new IllegalStateException(s"Expected node, got leaf at $path")
      case Tree.Node(tries) => getOverrideFromNode(label, tries)
    }
  }

  private def getOverrideFromNode[T](label: String, tries: Map[String, Tree[T]]) = {
    tries.get(label) match {
      case Some(Tree.Leaf(v)) => Some(v)
      case _                  => None
    }
  }

  def getNextStep(label: String): DiffContext = {
    val currentPath = path :+ label
    (getNextOverride(label, overrides), getNextOverride(label, matcherOverrides)) match {
      case (Some(d), Some(m)) => DiffContext(d, currentPath, m)
      case (None, Some(m))    => DiffContext(Tree.empty, currentPath, m)
      case (Some(d), None)    => DiffContext(d, currentPath, Tree.empty)
      case (None, None)       => DiffContext(Tree.empty, currentPath, Tree.empty)
    }
  }

  private def getNextOverride[T](label: String, tree: Tree[T]) = {
    tree match {
      case Tree.Leaf(_)     => None
      case Tree.Node(tries) => tries.get(label)
    }
  }
}

object DiffContext {
  val Empty: DiffContext = DiffContext(Tree.empty, List.empty, Tree.empty)
  def atPath(path: FieldPath, mod: Diff[Any] => Diff[Any]): DiffContext =
    Empty.copy(overrides = Tree.fromList(path, mod))
  def atPath(path: FieldPath, matcher: ObjectMatcher[_]): DiffContext =
    Empty.copy(matcherOverrides = Tree.fromList(path, matcher))
}

sealed trait Tree[T] {
  def merge(tree: Tree[T]): Tree[T]
}
object Tree {
  def empty[T]: Node[T] = Tree.Node[T](Map.empty)

  case class Leaf[T](v: T) extends Tree[T] {
    override def merge(tree: Tree[T]): Tree[T] = tree
  }
  case class Node[T](tries: Map[String, Tree[T]]) extends Tree[T] {
    override def merge(tree: Tree[T]): Tree[T] = {
      tree match {
        case Leaf(v) => Leaf(v)
        case Node(otherTries) =>
          val keys = tries.keySet ++ otherTries.keySet
          Node(keys.map { k =>
            k -> ((tries.get(k), otherTries.get(k)) match {
              case (Some(t1), Some(t2)) => t1.merge(t2)
              case (Some(t1), None)     => t1
              case (None, Some(t2))     => t2
              case (None, None)         => throw new IllegalStateException("cannot happen")
            })
          }.toMap)
      }
    }
  }
  def fromList[T](path: FieldPath, obj: T): Tree[T] = {
    path.reverse.foldLeft(Leaf(obj): Tree[T])((acc, item) => Node(Map(item -> acc)))
  }
}
