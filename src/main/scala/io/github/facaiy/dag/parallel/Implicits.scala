package io.github.facaiy.dag.parallel

import scala.concurrent.{Await, ExecutionContext, Future}

import io.github.facaiy.dag.core.{DAGNode, InputNode, InternalNode, LazyCell}

/**
 * Created by facai on 6/2/17.
 */
object Implicits {
  import scala.language.implicitConversions

  implicit def asFutureCell[K, V](nodes: Seq[DAGNode[K, V]]): FutureCell[K, V] = FutureCell(nodes)

  implicit def asLazyFuture[A](lc: LazyCell[Future[A]]): LazyFuture[A] = LazyFuture(lc)

  case class FutureCell[K, V](nodes: Seq[DAGNode[K, V]]) {
    def toParallel(implicit executor: ExecutionContext): Seq[DAGNode[K, Future[V]]] = {

      def toFutureCell(n: DAGNode[K, V]): DAGNode[K, Future[V]] =
        n match {
          case InputNode(k, f) =>
            // TODO(facai), use `compose` to combine `f` and `Future.apply`
            val g = () => Future(f())(executor)
            InputNode(k, g)
          case InternalNode(k, ds, f) =>
            val g = (xs: Seq[Future[V]]) => Future.sequence(xs)(implicitly, executor).map(f)
            InternalNode(k, ds, g)
        }

      nodes.map(toFutureCell)
    }
  }

  case class LazyFuture[A](lc: LazyCell[Future[A]]) {
    import scala.concurrent.duration._

    def getValue: A = getValue(10)

    def getValue(timeOut: Int): A =
      Await.result(lc.get(), timeOut.seconds)
  }
}
