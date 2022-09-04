package org.topl.traffic.protocol.models

class WeightedGraph[V](adjacencyList: Map[V, List[WeightedEdge[V]]]) extends Graph[V] {
  override def vertices: List[V] = adjacencyList.keys.toList

  override def edges: List[(source, destination)] = adjacencyList.flatMap { case (v, edgeList) =>
    edgeList.map(edge => (v, edge.destination))
  }.toList

  def addEdge(a: V, weightedEdge: WeightedEdge[V]): WeightedGraph[V] = {
    val aNeighbours = weightedEdge +: adjacencyList.getOrElse(a, List())
    new WeightedGraph[V](adjacencyList + (a -> aNeighbours))
  }

  override def addEdge(a: V, b: V): WeightedGraph[V] = addEdge(a, WeightedEdge(b, 0.0))

  override def neighbours(vertex: V): List[V] = adjacencyList.getOrElse(vertex, List()).map(_.destination)

  def neighboursWithWeight(vertex: V): List[WeightedEdge[V]] = adjacencyList.getOrElse(vertex, List())
}
