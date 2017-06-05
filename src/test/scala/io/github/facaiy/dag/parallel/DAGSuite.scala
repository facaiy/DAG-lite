package io.github.facaiy.dag.parallel

import io.github.facaiy.dag.core.{InputNode, InternalNode}
import io.github.facaiy.dag.parallel.Implicits._
import org.scalatest.FunSpec

/**
 * Created by facai on 5/10/17.
 */
class DAGSuite extends FunSpec {
  describe("For lazy and concurrent network") {
    it("nodes are only be evaluated once.") {
      val nodes = Seq(InputNode("input", () => System.currentTimeMillis()))
      val fm = nodes.toLazyNetwork

      val before = fm("input").getValue
      Thread.sleep(1)
      val after = fm("input").getValue

      assert(before === after)
    }

    it("nodes run in parallel.") {
      import java.util.concurrent.Executors
      import scala.concurrent.ExecutionContext

      implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

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
      val process1 = InternalNode(
        "process1",
        Seq("input1", "input2", "input3"),
        process("process1"))
      val nodes = Seq(input1, input2, input3, process1)

      val fm = nodes.toLazyNetwork

      assert(
        fm("process1").getValue
          .sortBy(_.start)
          .sliding(2)
          .exists(x => x(1).start < x(0).end))
    }
  }
}
