package io.github.facaiy.dag.serial

import scala.concurrent.duration.Duration

import io.github.facaiy.dag.core.{DAGNode, InputNode, InternalNode, LazyCell}
import io.github.facaiy.dag.core.LazyCell._
import io.github.facaiy.dag.Result

/**
 * Created by facai on 6/2/17.
 */
object Implicits { self =>
  private[dag] def toLazyNetwork[K, V](nodes: Seq[DAGNode[K, V]]): Map[K, LazyCell[V]] = {
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

  import scala.language.implicitConversions

  implicit class Nodes[K, V](nodes: Seq[DAGNode[K, V]]) {
    def toLazyNetwork: Map[K, LazyCell[V]] = self.toLazyNetwork(nodes)
  }

  implicit class SerResult[A](lc: LazyCell[A]) extends Result[A] {
    def getValue: A = lc.get()

    @deprecated("Invalid argument, use `getValue` instead.", "0.2.0")
    def getValue(duration: Duration): A = getValue
  }
}
