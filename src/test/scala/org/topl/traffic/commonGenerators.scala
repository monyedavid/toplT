package org.topl.traffic

import org.scalacheck.Gen

object commonGenerators {

  def forSingleInstance[T](gen: Gen[T])(test: T => Any): Any = {
    val numRetries = 1000
    @scala.annotation.tailrec
    def go(retries: Int): Any =
      gen.sample match {
        case Some(sample) =>
          test(sample)
        case None if retries <= numRetries =>
          go(retries + 1)
        case None =>
          throw new Exception(s"Gave up after $numRetries attempts")
      }
    go(0)
  }
}
