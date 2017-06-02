package io.github.facaiy.DAG.parallel

import scala.concurrent.{Await, ExecutionContext, Future}

import io.github.facaiy.DAG.{DAG => ParentDAG}
import io.github.facaiy.DAG.core.{DAGNode, InputNode, InternalNode, LazyCell}
import io.github.facaiy.DAG.serial.DAG.Nodes

/**
 * Created by facai on 6/2/17.
 */
object DAG extends ParentDAG {
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

object Implicits {
  import DAG._

  import scala.language.implicitConversions

  // TODO(facai), duplication of serial.DAG.asNodesOps
  implicit def asNodes[K, V](nodes: Seq[DAGNode[K, V]]): Nodes[K, V] = Nodes(nodes)

  implicit def asFutureCell[K, V](nodes: Seq[DAGNode[K, V]]): FutureCell[K, V] = FutureCell(nodes)

  implicit def asLazyFuture[A](lc: LazyCell[Future[A]]): LazyFuture[A] = LazyFuture(lc)
}
