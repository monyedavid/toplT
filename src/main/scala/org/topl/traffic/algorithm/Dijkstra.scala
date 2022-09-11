package org.topl.traffic.algorithm

import org.topl.traffic.protocol.models.{WeightedEdge, WeightedGraph}

import scala.annotation.tailrec

class Dijkstra[V](graph: WeightedGraph[V], sourceNode: V) {

  type Node = V

  // step: 0 => Source node distance is set to 0 @ value {sDistances}
  val sTransitTimes: Map[Node, Double] = graph.vertices.map(_ -> Double.MaxValue).toMap + (sourceNode -> 0.0)

  // shortestPathTailRec :)
  // this will build the best shortest path for nodes in graph with a source node as starting point
  // actual Paths may not exist between a given source-node & a destination node
  @tailrec
  final def shortestPathTRec(step: ShortStep[V]): ShortStep[V] =
    // step:1  => get min in unprocessed set
    step.extractMin match {
      case Some((node, currentDistance)) =>
        // graph.neighboursWithWeight(node) => Collect neighbours of current minimum node
        val newTransitTimes = graph.neighboursWithWeight(node).collect {
          case WeightedEdge(destination, weight) // @destination => neighbouring node & its weight
              if step.transitTimes
                // step.transitTimes.get(destination) => shortest possible weight
                .get(destination)
                // check if @destination current shortest weight is < currentDistance(from min node in unprocessed set) + weight(of destination)
                .exists(_ > currentDistance + weight) =>
            (destination, currentDistance + weight)
        }

        val newParents = newTransitTimes.map { case (m, _) => m -> node }
        shortestPathTRec(
          ShortStep(step.parents ++ newParents, step.unProcessed - node, step.transitTimes ++ newTransitTimes)
        )
      case None => step
    }

}

object Dijkstra {
  def apply[V](graph: WeightedGraph[V], sourceNode: V): Dijkstra[V] = new Dijkstra[V](graph, sourceNode)
}
