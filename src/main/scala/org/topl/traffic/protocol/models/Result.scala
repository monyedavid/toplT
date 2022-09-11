package org.topl.traffic.protocol.models

final case class Result[V](path: List[V], totalTransitTime: Double)

object Result {
  def empty[V]: Result[V] = Result(List(), 0.0)
}
