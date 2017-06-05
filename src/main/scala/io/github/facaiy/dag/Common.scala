package io.github.facaiy.dag

import scala.concurrent.duration.Duration

/**
 * Created by facai on 6/5/17.
 */
trait Result[A] {
  def getValue: A

  def getValue(duration: Duration): A
}
