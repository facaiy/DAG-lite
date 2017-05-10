package io.github.facaiy.DAG

import java.util.concurrent.ConcurrentHashMap

import org.scalatest.FunSpec

/**
 * Created by facai on 5/10/17.
 */
class DAGSuite extends FunSpec {
  describe("For node of the network,") {
    it("is lazy and be evaluated only once.") {
      /* ------- records for method execute times --------- */
      val records = new ConcurrentHashMap[String, Int]()

      def incr(id: String): Unit = {
        val t = records.getOrDefault(id, 0) + 1
        records.put(id, t)
      }

      /* ------- helper method --------- */
      /* input function: () => A */
      def read(name: String, value: Int)(): Int = {
        incr(name)
        value
      }

      /* process function: A => A */
      def processOneDepend(name: String)(s: Int): Int = {
        incr(name)
        s * 2
      }

      /* process function: Seq[A] => A */
      def processMultiDepends(name: String)(ss: Seq[Int]): Int = {
        incr(name)
        ss.sum
      }

      /* output function: Seq[A] => () */
      def write(name: String)(ss: Seq[Int]): Unit = {
        incr(name)
      }

      /* ------- build network --------- */
      val input1 = InputNode("i1", read("i1", 1))
      val input2 = InputNode("i2", read("i2", 2))
      val input3 = InputNode("i3", read("i3", 3))
      val process1 = ProcessNode("p1", "i1", processOneDepend("p1") _)
      val process2 = ProcessNode("p2", Seq("i2", "i3"), processMultiDepends("p2") _)
      val output1 = OutputNode("o1", Seq("p2", "i2"), write("o1") _)
      val output2 = OutputNode("o2", Seq("p2", "p1"), write("o2") _)

      val nodes = Seq(input1, input2, input3, process1, process2, output1, output2)

      val m: Map[String, LazyCell[Int]] = DAGNode.toLazyNetWork(nodes)
      assert(m.size === 7)

      m("o1").getValue()
      assert(records.values().toArray().forall(_ === 1))
      assert(records.size === 4)
      assert(records.containsKey("p2"))
      assert(records.containsKey("o1"))
      assert(records.containsKey("i2"))
      assert(records.containsKey("i3"))

      m("o2").getValue()
      assert(records.values().toArray().forall(_ === 1))
      assert(records.size === m.size)

      assert(m("p1").getValue() === 2)
      assert(m("p2").getValue() === 5)
      assert(records.values().toArray().forall(_ === 1))
    }
  }
}
