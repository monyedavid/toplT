package org.topl.traffic.algorithm

import org.topl.traffic.protocol.models.{WeightedEdge, WeightedGraph}

class Dijkstra[V](graph: WeightedGraph[V], startNode: V) {

  val sDistances: Map[V, Double] = graph.vertices.map(_ -> Double.MaxValue).toMap + (startNode -> 0)

  def shortestPath(step: ShortStep[V]): ShortStep[V] =
    step.extractMin
      .map { case (node, currentDistance) =>
        val newDistance = graph.neighboursWithWeight(node).collect {
          case WeightedEdge(destination, weight)
              if step.distances.get(destination).exists(_ > currentDistance + weight) =>
            (destination, currentDistance + weight)
        }

        val newParents = newDistance.map { case (m, _) => m -> node }

        // update parent map, remove node from unProcessed node set, update distance map
        shortestPath(step =
          ShortStep(step.parents ++ newParents, step.unProcessed - node, step.distances ++ newDistance)
        )
      }
      .getOrElse(step)

}

object Dijkstra {
  def apply[V](graph: WeightedGraph[V], startNode: V): Dijkstra[V] = new Dijkstra[V](graph, startNode)
}
