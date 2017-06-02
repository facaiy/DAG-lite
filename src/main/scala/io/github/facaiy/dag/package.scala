package io.github.facaiy

import io.github.facaiy.dag.core.{DAGNode, InputNode, InternalNode, LazyCell}
import io.github.facaiy.dag.core.LazyCell._

/**
 * Created by facai on 6/2/17.
 */
package object dag { self =>
  def toLazyNetwork[K, V](nodes: Seq[DAGNode[K, V]]): Map[K, LazyCell[V]] = {
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

  case class Nodes[K, V](nodes: Seq[DAGNode[K, V]]) {
    def toLazyNetwork: Map[K, LazyCell[V]] = self.toLazyNetwork(nodes)
  }

  import scala.language.implicitConversions

  implicit def asNodes[K, V](nodes: Seq[DAGNode[K, V]]): Nodes[K, V] = Nodes(nodes)
}