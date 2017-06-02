package io.github.facaiy.DAG.core

/**
 * Created by facai on 5/5/17.
 */
case class LazyCell[+A](get: () => A) {
  import LazyCell._

  def map[B](f: A => B): LazyCell[B] = lazyCell(f(get()))

  def flatMap[B](f: A => LazyCell[B]): LazyCell[B] = map(f(_).get())

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

  def sequence[A](as: Seq[LazyCell[A]]): LazyCell[Seq[A]] = lazyCell(as.map(_.get()))
}
