package io.github.facaiy.DAG

/**
 * Created by facai on 5/5/17.
 */
case class LazyCell[+A](getValue: () => A) {
  import LazyCell._

  def map[B](f: A => B): LazyCell[B] = lazyCell(f(getValue()))

  def flatMap[B](f: A => LazyCell[B]): LazyCell[B] = map(f(_).getValue())

  def map2[B, C](that: LazyCell[B])(f: (A, B) => C): LazyCell[C] =
    for {
      a <- this
      b <- that
    } yield f(a, b)
}

object LazyCell {
  def lazyCell[A](f: => A): LazyCell[A] = {
    lazy val value = f
    LazyCell(() => value)
  }

  def sequence[A](as: Seq[LazyCell[A]]): LazyCell[Seq[A]] = lazyCell(as.map(_.getValue()))

  def sequencePar[A](as: Seq[LazyCell[A]]): LazyCell[Seq[A]] = {
    // lazyCell(as.par.map(_.getValue()).seq)
    Test.lazyCell(test(as))
  }

  def test[A](as: Seq[LazyCell[A]]): Seq[A] = {
    import scala.concurrent._
    import scala.concurrent.ExecutionContext.Implicits.global
    import scala.concurrent.duration._

    // import java.util.concurrent.Executors
    // implicit val ec = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(20))

    val res: Future[Seq[A]] = Future.sequence(as.map(x => Future(blocking(x.getValue()))))
      Await.result(res, 12 seconds)
  }
}

object Test {
  def lazyCell[A](f: => A): LazyCell[A] = {
    lazy val value = f
    LazyCell(() => value)
  }
}
