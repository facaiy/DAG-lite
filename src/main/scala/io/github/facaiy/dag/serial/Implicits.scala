package io.github.facaiy.dag.serial

import io.github.facaiy.dag.core.LazyCell

/**
 * Created by facai on 6/2/17.
 */
object Implicits {
  import scala.language.implicitConversions

  implicit class LazyCellOps[A](lc: LazyCell[A]) {
    def getValue: A = lc.get()
  }
}
