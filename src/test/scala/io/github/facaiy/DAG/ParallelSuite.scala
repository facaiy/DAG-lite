package io.github.facaiy.DAG

import org.scalatest.FunSpec

/**
 * Created by facai on 5/24/17.
 */
class ParallelSuite extends FunSpec {
  describe("test") {
    it("parallel") {
      val ls: Seq[LazyCell[Int]] = Range(0, 10).map(x => LazyCell.lazyCell{Thread.sleep(1000); x})

      val lz = LazyCell.sequencePar(ls)

      val t1 = System.nanoTime
      println(lz.getValue())
      val duration = (System.nanoTime - t1) / 1e9d
      println(duration)
    }
  }
}
