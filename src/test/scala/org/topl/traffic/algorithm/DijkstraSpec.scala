package org.topl.traffic.algorithm

import org.scalatest.{PrivateMethodTester, TryValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.topl.traffic.commonGenerators.forSingleInstance
import org.topl.traffic.protocol.models.Intersection
import org.topl.traffic.protocol.models.generators.testGraphGen

class DijkstraSpec extends AnyFlatSpec with should.Matchers with TryValues with PrivateMethodTester {
  "Dijkstra" should "correctly generate shortest path from source node to all nodes in a graph" in {
    import Intersection._
    forSingleInstance(testGraphGen) { tG =>
      val dijkstra = new Dijkstra(tG, "M1".fromStringUnsafe)

      val ss = dijkstra.shortestPath(
        ShortStep(unProcessed = tG.vertices.toSet, distances = dijkstra.sDistances)
      )

      tG.vertices.map(node => PathT.extractSPaths(node, ss.parents).reverse) should contain theSameElementsAs
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
}
