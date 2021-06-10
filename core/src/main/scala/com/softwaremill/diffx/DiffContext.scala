package com.softwaremill.diffx

case class DiffContext(overrides: Tree, path: FieldPath) {
  def merge(other: DiffContext): DiffContext = {
    DiffContext(overrides.merge(other.overrides), List.empty)
  }

  def getOverride(label: String): Option[Diff[_]] = {
    overrides match {
      case Tree.Leaf(_) => throw new IllegalStateException(s"Expected node, got leaf at $path")
      case Tree.Node(tries) =>
        val currentPath = path :+ label
        tries.get(label) match {
          case Some(Tree.Leaf(v)) => Some(v)
          case Some(Tree.Node(_)) => None
          case None               => None
        }
    }
  }
  def getNextStep(label: String): DiffContext = {
    overrides match {
      case Tree.Leaf(_) => throw new IllegalStateException(s"Expected node, got leaf at $path")
      case Tree.Node(tries) =>
        val currentPath = path :+ label
        tries.get(label) match {
          case Some(value) => DiffContext(value, currentPath)
          case None        => DiffContext.Empty
        }
    }
  }
}

object DiffContext {
  val Empty: DiffContext = DiffContext(Tree.Node(Map.empty), List.empty)
  def atPath(path: FieldPath, diff: Diff[_]) = DiffContext(Tree.fromList(path, diff), List.empty)
}

sealed trait Tree {
  def merge(tree: Tree): Tree
}
object Tree {
  case class Leaf(v: Diff[_]) extends Tree {
    override def merge(tree: Tree): Tree = tree
  }
  case class Node(tries: Map[String, Tree]) extends Tree {
    override def merge(tree: Tree): Tree = {
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
  def fromList(path: FieldPath, diff: Diff[_]): Tree = {
    path.reverse.foldLeft(Leaf(diff): Tree)((acc, item) => Node(Map(item -> acc)))
  }
}
