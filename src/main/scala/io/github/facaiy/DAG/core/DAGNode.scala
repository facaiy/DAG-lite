package io.github.facaiy.DAG.core

import scala.concurrent.{Await, ExecutionContext, Future}

import io.github.facaiy.DAG.core.LazyCell.{lazyCell, sequence}

/**
 * Created by facai on 5/10/17.
 */
sealed trait DAGNode[K, V] {
  def name: K
}

case class InputNode[K, V](override val name: K,
                           transFunc: () => V) extends DAGNode[K, V]

private[DAG] case class InternalNode[K, V](override val name: K,
                                      depends: Seq[K],
                                      reduceFunc: Seq[V] => V) extends DAGNode[K, V]

object ProcessNode {
  def apply[K, V](name: K, depends: Seq[K], reduceFunc: Seq[V] => V): DAGNode[K, V] =
    InternalNode(name, depends, reduceFunc)

  def apply[K, V](name: K, depend: K, transFunc: V => V): DAGNode[K, V] =
    apply(name, Seq(depend), (ss: Seq[V]) => transFunc(ss.head))
}

object OutputNode {
  def apply[K, V](name: K, depends: Seq[K], reduceFunc: Seq[V] => Unit): DAGNode[K, V] =
    InternalNode(name, depends, (ss: Seq[V]) => { reduceFunc(ss); ss.head })

  def apply[K, V](name: K, depend: K, transFunc: V => Unit): DAGNode[K, V] =
    apply(name, Seq(depend), (ss: Seq[V]) => transFunc(ss.head))
}
