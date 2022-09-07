package org.topl.traffic.algorithm

import scala.collection.mutable

// todo: use PQ
case class ShortStepPQ[V](
  parents: Map[V, V] = Map[V, V](),
  unProcessed: mutable.PriorityQueue[(V, Double)]
) {
  def extractMin: Option[(V, Double)] = None
}
