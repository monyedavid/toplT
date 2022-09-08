package org.topl.traffic.algorithm

import org.scalatest.{PrivateMethodTester, TryValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.topl.traffic.protocol.models.{Graph, Intersection, WeightedEdge, WeightedGraph}

class DijkstraSpec extends AnyFlatSpec with should.Matchers with TryValues with PrivateMethodTester {
  "Dijkstra" should "correctly generate shortest path from source node to all nodes in a graph" in {
    import Intersection._
    val graph: WeightedGraph[Intersection] = Graph[Intersection]()
      .addEdge("M1".fromStringUnsafe, WeightedEdge("G1".fromStringUnsafe, 60))
      .addEdge("M1".fromStringUnsafe, WeightedEdge("C1".fromStringUnsafe, 160))
      .addEdge("M1".fromStringUnsafe, WeightedEdge("D1".fromStringUnsafe, 170))
      .addEdge("M1".fromStringUnsafe, WeightedEdge("J1".fromStringUnsafe, 50))
      .addEdge("M1".fromStringUnsafe, WeightedEdge("B1".fromStringUnsafe, 250))
      .addEdge("M1".fromStringUnsafe, WeightedEdge("D2".fromStringUnsafe, 360))
      .addEdge("G1".fromStringUnsafe, WeightedEdge("C2".fromStringUnsafe, 20))
      .addEdge("C2".fromStringUnsafe, WeightedEdge("C1".fromStringUnsafe, 40))
      .addEdge("C1".fromStringUnsafe, WeightedEdge("C1".fromStringUnsafe, 210))
      .addEdge("D1".fromStringUnsafe, WeightedEdge("S1".fromStringUnsafe, 320))
      .addEdge("S1".fromStringUnsafe, WeightedEdge("B1".fromStringUnsafe, 210))
      .addEdge("B1".fromStringUnsafe, WeightedEdge("S1".fromStringUnsafe, 260))
      .addEdge("B1".fromStringUnsafe, WeightedEdge("D2".fromStringUnsafe, 90))
      .addEdge("D2".fromStringUnsafe, WeightedEdge("B1".fromStringUnsafe, 140))
      .addEdge("J1".fromStringUnsafe, WeightedEdge("D1".fromStringUnsafe, 110))
      .addEdge("J1".fromStringUnsafe, WeightedEdge("K2".fromStringUnsafe, 190))
      .addEdge("K2".fromStringUnsafe, WeightedEdge("J1".fromStringUnsafe, 160))
      .addEdge("K2".fromStringUnsafe, WeightedEdge("D2".fromStringUnsafe, 90))

    val dijkstra = new Dijkstra(graph, "M1".fromStringUnsafe)

    val ss = dijkstra.shortestPath(
      ShortStep(unProcessed = graph.vertices.toSet, distances = dijkstra.sDistances)
    )

    graph.vertices.map(node => PathT.extractSPaths(node, ss.parents).reverse) should contain theSameElementsAs
    List(
      List("M1".fromStringUnsafe, "G1".fromStringUnsafe),
      List("M1".fromStringUnsafe),
      List("M1".fromStringUnsafe, "J1".fromStringUnsafe, "D1".fromStringUnsafe, "S1".fromStringUnsafe),
      List("M1".fromStringUnsafe, "B1".fromStringUnsafe),
      List("M1".fromStringUnsafe, "J1".fromStringUnsafe),
      List("M1".fromStringUnsafe, "J1".fromStringUnsafe, "K2".fromStringUnsafe),
      List("M1".fromStringUnsafe, "G1".fromStringUnsafe, "C2".fromStringUnsafe),
      List("M1".fromStringUnsafe, "G1".fromStringUnsafe, "C2".fromStringUnsafe, "C1".fromStringUnsafe),
      List("M1".fromStringUnsafe, "J1".fromStringUnsafe, "D1".fromStringUnsafe),
      List("M1".fromStringUnsafe, "J1".fromStringUnsafe, "K2".fromStringUnsafe, "D2".fromStringUnsafe)
    )

  }
}
