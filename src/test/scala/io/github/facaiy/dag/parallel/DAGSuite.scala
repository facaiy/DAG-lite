package io.github.facaiy.dag.parallel

import io.github.facaiy.dag.core.{InputNode, ProcessNode}
import io.github.facaiy.dag.parallel.Implicits._
import org.scalatest.FunSpec

/**
 * Created by facai on 5/10/17.
 */
class DAGSuite extends FunSpec {
  describe("For lazy and concurrent network") {
    describe("nodes are only be evaluated once") {
      it("when fetch in sequence") {
        val nodes = Seq(InputNode("input", () => System.currentTimeMillis()))
        val fm = nodes.toLazyNetwork
  
        val before = fm("input").getValue
        Thread.sleep(1)
        val after = fm("input").getValue
  
        assert(before === after)
      }
      
      it("when fetch in parallel") {
        val nodes = Seq(
          InputNode("input", () => { Thread.sleep(1000); System.currentTimeMillis() }), // delay
          ProcessNode("fetch1", "input", (x: Long) => x),
          ProcessNode("fetch2", "input", (x: Long) => x),
          ProcessNode("fetch3", "input", (x: Long) => x),
          ProcessNode("merge", Seq("fetch1", "fetch2", "fetch3"),
                      (xs: Seq[Long]) => xs.toSet.size.toLong))
  
        val fm = nodes.toLazyNetwork
        assert(fm("merge").getValue === 1)
      }
    }

    it("nodes run in parallel") {
      import java.util.concurrent.Executors
      import scala.concurrent.ExecutionContext

      case class TimeRecord(id: String, start: Long, end: Long)

      /* ------- helper method --------- */
      def read(id: String, delayStart: Int = 10, runTime: Int = 1000)(): Seq[TimeRecord] = {
        Thread.sleep(delayStart)
        val start = System.currentTimeMillis()
        Thread.sleep(runTime)
        val end = System.currentTimeMillis()

        List(TimeRecord(id, start, end))
      }

      def process(id: String)(st: Seq[Seq[TimeRecord]]): Seq[TimeRecord] =
        read(id)() ++ st.reduce(_ ++ _)

      /* ------- build network --------- */
      val input1 = InputNode("input1", read("input1", 0))
      val input2 = InputNode("input2", read("input2", 100))
      val input3 = InputNode("input3", read("input3", 50))
      val process1 = ProcessNode(
        "process1",
        Seq("input1", "input2", "input3"),
        process("process1") _)
      val nodes = Seq(input1, input2, input3, process1)

      val executor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
      val fm = nodes.toLazyNetwork(executor)

      assert(
        fm("process1").getValue
          .sortBy(_.start)
          .sliding(2)
          .exists(x => x(1).start < x(0).end))
    }
  }
}
