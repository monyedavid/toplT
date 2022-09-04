package org.topl.traffic.protocol.models

trait Graph[V] {
  type source      = V
  type destination = V
  type adList      = List[V]

  def vertices: adList

  def edges: List[(source, destination)]

  def addEdge(a: V, b: V): Graph[V]

  def neighbours(vertex: V): adList
}

object Graph {
  def apply[V](): WeightedGraph[V] = new WeightedGraph[V](Map())

  def apply[V](adjacencyList: Map[V, List[WeightedEdge[V]]]): WeightedGraph[V] = new WeightedGraph[V](adjacencyList)

}
