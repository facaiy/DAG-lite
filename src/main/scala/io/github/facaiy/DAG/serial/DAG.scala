package io.github.facaiy.DAG.serial

import io.github.facaiy.DAG.{DAG => ParentDAG}
import io.github.facaiy.DAG.core.{DAGNode, InputNode, InternalNode, LazyCell}
import io.github.facaiy.DAG.core.LazyCell._

/**
 * Created by facai on 6/2/17.
 */
object DAG extends ParentDAG {
  import scala.language.implicitConversions

  implicit def asNodesOps[K, V](nodes: Seq[DAGNode[K, V]]): Nodes[K, V] = Nodes(nodes)
  implicit def asLazyCellOps[A](l: LazyCell[A]): LazyCellOps[A] = LazyCellOps(l)

  case class Nodes[K, V](nodes: Seq[DAGNode[K, V]]) {
    def toLazyNetWork: Map[K, LazyCell[V]] = {
      lazy val nodesMap: LazyCell[Map[K, LazyCell[V]]] =
        lazyCell(nodes.map(toLazyCell).toMap)

      def toLazyCell(n: DAGNode[K, V]): (K, LazyCell[V]) =
        n match {
          case InputNode(k, f) => k -> lazyCell(f())
          case InternalNode(k, ds, f) =>
            k -> nodesMap.flatMap { m =>
              val inputs = ds.map(d => m.getOrElse(d, throw new NoSuchElementException(d.toString)))
              sequence(inputs).map(f)
            }
        }

      nodesMap.get()
    }
  }

  case class LazyCellOps[A](lc: LazyCell[A]) {
    def getValue(): A = lc.get()
  }
}
