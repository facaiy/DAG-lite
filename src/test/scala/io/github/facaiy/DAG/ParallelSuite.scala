package io.github.facaiy.DAG

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import org.scalatest.FunSpec

/**
 * Created by facai on 5/24/17.
 */
class ParallelSuite extends FunSpec {
  describe("parallel") {
    it("lazy cell") {
      val t1 = System.nanoTime

      val ls: Seq[LazyCell[Future[Int]]] =
        Range(0, 10).map(x => LazyCell.lazyCell(Future{Thread.sleep(1000); x}))

      // val lz = LazyCell.sequencePar(ls)
      val lz: LazyCell[Future[Seq[Int]]] = LazyCell.sequence(ls).map(x => Future.sequence(x))

      println(Await.result(lz.getValue(), 12 seconds))

      val duration = (System.nanoTime - t1) / 1e9d
      println(duration)
    }

    it("primitive type") {
      val t1 = System.nanoTime

      lazy val ls: Seq[Future[Int]] = Range(0, 10).map(x => Future{Thread.sleep(1000); x})
      lazy val lz = Future.sequence(ls)

      println(Await.result(lz, 12 seconds))

      val duration = (System.nanoTime - t1) / 1e9d
      println(duration)
    }
  }
}
