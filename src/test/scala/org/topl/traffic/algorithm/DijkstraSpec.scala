package org.topl.traffic.algorithm

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.topl.traffic.commonGenerators.forSingleInstance
import org.topl.traffic.protocol.models.Intersection
import org.topl.traffic.protocol.models.generators.testGraphGen

class DijkstraSpec extends AnyFlatSpec with should.Matchers {
  "Dijkstra" should "correctly generate transitTimes, parents & process all nodes in Set" in {
    import Intersection._
    forSingleInstance(testGraphGen) { tG =>
      val sourceNode = "M1".fromStringUnsafe
      val dijkstra   = new Dijkstra(tG, sourceNode)

      val ss = dijkstra.shortestPathTRec(
        ShortStep(unProcessed = tG.vertices.toSet, transitTimes = dijkstra.sTransitTimes)
      )

      ss.unProcessed should be(Set[Intersection]())
      ss.parents.toList should contain theSameElementsAs Map(
        "G1".fromStringUnsafe -> "M1".fromStringUnsafe,
        "B1".fromStringUnsafe -> "M1".fromStringUnsafe,
        "K2".fromStringUnsafe -> "J1".fromStringUnsafe,
        "D1".fromStringUnsafe -> "J1".fromStringUnsafe,
        "S1".fromStringUnsafe -> "D1".fromStringUnsafe,
        "J1".fromStringUnsafe -> "M1".fromStringUnsafe,
        "C2".fromStringUnsafe -> "G1".fromStringUnsafe,
        "C1".fromStringUnsafe -> "C2".fromStringUnsafe,
        "D2".fromStringUnsafe -> "K2".fromStringUnsafe
      ).toList
      ss.transitTimes.toList should contain theSameElementsAs Map[Intersection, Double](
        "G1".fromStringUnsafe -> 60,
        "B1".fromStringUnsafe -> 250,
        "K2".fromStringUnsafe -> 240,
        "D1".fromStringUnsafe -> 160,
        "M1".fromStringUnsafe -> 0,
        "S1".fromStringUnsafe -> 480,
        "J1".fromStringUnsafe -> 50,
        "C2".fromStringUnsafe -> 80,
        "C1".fromStringUnsafe -> 120,
        "D2".fromStringUnsafe -> 330
      ).toList
    }
  }

}
