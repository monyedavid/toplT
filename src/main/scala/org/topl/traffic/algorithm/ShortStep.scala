package org.topl.traffic.algorithm

import scala.util.Try

case class ShortStep[V](parents: Map[V, V] = Map[V, V](), unProcessed: Set[V], distances: Map[V, Double]) {
  def extractMin: Option[(V, Double)] = Try(unProcessed.minBy(n => distances(n))).toOption.map(n => (n, distances(n)))
}
