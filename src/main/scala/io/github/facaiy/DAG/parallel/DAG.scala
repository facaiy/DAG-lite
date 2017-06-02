package io.github.facaiy.DAG.parallel

import scala.concurrent.{Await, ExecutionContext, Future}

import io.github.facaiy.DAG.{DAG => ParentDAG}
import io.github.facaiy.DAG.core.{DAGNode, InputNode, InternalNode, LazyCell}
import io.github.facaiy.DAG.serial.DAG.asNodesOps

/**
 * Created by facai on 6/2/17.
 */
object DAG extends ParentDAG {
  def toLazyNetwork[K, V](nodes: Seq[DAGNode[K, V]])
                         (implicit executor: ExecutionContext): Map[K, LazyCell[Future[V]]] = {

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

    nodes.map(toFutureCell).toLazyNetWork
  }

  import scala.language.implicitConversions

  case class FutureCell[K, V](nodes: Seq[DAGNode[K, V]]) {
  }

  implicit def asLazyFuture[A](lc: LazyCell[Future[A]]): LazyFuture[A] = LazyFuture(lc)

  case class LazyFuture[A](lc: LazyCell[Future[A]]) {
    import scala.concurrent.duration._

    def getValue(timeOut: Int = 10): A =
      Await.result(lc.get(), timeOut.seconds)
  }
}
