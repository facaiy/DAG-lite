# DAG-lite
An experimental DAG library with functional programming technology.

features:
+ lazy evaluation.
+ concurrent computing.


### Install

1. maven
   ```
   <dependency>
     <groupId>io.github.facaiy</groupId>
     <artifactId>DAG-lite</artifactId>
     <version>0.1.0</version>
   </dependency>
   ```

2. sbt: todo.


### Usage

1. A simple example:
   ```scala
     def read[A](): A
     def process1[A](a: A): A
     def process2[A](as: Seq[A]): A
     def write1[A](a: A): Unit
     def write2[A](as: Seq[A]): Unit

     // create nodes
     // InputNode(uid, function)
     val inputNode1 = InputNode("input1", read)
     val inputNode2 = InputNode("input2", read)
     // ProcessNode(uid, dependencyUids, function)
     val internalNode1 = ProcessNode("process1", "input1", process1)
     val internalNode2 = ProcessNode("process2", Seq("input1", "input2") process2)
     // OutputNode(uid, dependencyUids, function)
     val outputNode1 = OutputNode("output1", "process1", write1)
     val outputNode2 = OutputNode("output2", Seq("process1", "process2"), write2)

     val nodes = Seq(inputNode, internalNode1, internalNode2, outputNode1, outputNode2)

     // build network
     val lm = DAGNode.toLazyNetWork(nodes)
     /**
      * input1 ---> process1 ---> output1
      *         |             |
      *         v             v
      * input2 ---> process2 ---> output2
      */

     // run if needed
     lm("output1").getValue()
     lm("output2").getValue()
   ```

2. Experimental: use `toFutureNetWork` to run nodes in parallel.
   ```scala
     // build network
     val fm = DAGNode.toFutureNetWork(nodes)
     /**
      * input1 ---> process1 ---> output1
      *         |             |
      *         v             v
      * input2 ---> process2 ---> output2
      */

     // run if needed
     fm("output1").getFuture()
     fm("output2").getFuture()
   ```
