package org.topl.traffic.algorithm

import scala.util.Try

// parents => Map[Child, Parents]
// transitTimes => Map[Node, Int]; Int => Shortest accumulative weight leading to Node
case class ShortStep[V](parents: Map[V, V] = Map[V, V](), unProcessed: Set[V], transitTimes: Map[V, Double]) {

  def extractMin: Option[(V, Double)] =
    Try(unProcessed.minBy(n => transitTimes(n))).toOption.map(n => (n, transitTimes(n)))
}
