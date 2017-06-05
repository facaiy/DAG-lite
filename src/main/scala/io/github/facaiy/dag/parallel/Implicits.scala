package io.github.facaiy.dag.parallel

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.Duration

import io.github.facaiy.dag.core.{DAGNode, InputNode, InternalNode, LazyCell}

/**
 * Created by facai on 6/2/17.
 */
object Implicits { self =>
  private[dag] def toParallel[K, V](nodes: Seq[DAGNode[K, V]])
                                   (implicit executor: ExecutionContext
                                   ): Seq[DAGNode[K, Future[V]]] = {

    def toFutureCell(n: DAGNode[K, V]): DAGNode[K, Future[V]] =
      n match {
        case InputNode(k, f) =>
          // TODO(facai), use `compose` to combine `f` and `Future.apply`
          val g = () => Future(f())
          InputNode(k, g)
        case InternalNode(k, ds, f) =>
          val g = (xs: Seq[Future[V]]) => Future.sequence(xs).map(f)
          InternalNode(k, ds, g)
      }

    nodes.map(toFutureCell)
  }

  private[dag] def toLazyNetwork[K, V](nodes: Seq[DAGNode[K, V]])
                                      (executor: ExecutionContext): Map[K, LazyCell[Future[V]]] = {
    import io.github.facaiy.dag.serial

    serial.Implicits.toLazyNetwork(self.toParallel(nodes)(executor))
  }


  import scala.language.implicitConversions

  implicit class Nodes[K, V](nodes: Seq[DAGNode[K, V]]) {
    import scala.concurrent.ExecutionContext.Implicits.global

    def toLazyNetwork: Map[K, LazyCell[Future[V]]] = this.toLazyNetwork(global)

    def toLazyNetwork(executor: ExecutionContext): Map[K, LazyCell[Future[V]]] =
      self.toLazyNetwork(nodes)(executor)

  }

  implicit class Result[A](lc: LazyCell[Future[A]]) {
    def getValue: A = getValue(Duration.Inf)

    def getValue(duration: Duration): A =
      Await.result(lc.get(), duration)
  }
}
