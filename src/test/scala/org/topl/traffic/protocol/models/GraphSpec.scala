package org.topl.traffic.protocol.models

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.topl.traffic.commonGenerators.forSingleInstance
import org.topl.traffic.protocol.models.Intersection.RichInterSection
import org.topl.traffic.protocol.models.generators.emptyGraphGen

class GraphSpec extends AnyFlatSpec with should.Matchers {

  "Graph" should "add Edge" in {
    forSingleInstance(emptyGraphGen) { wg =>
      val graph = wg
        .addEdge("M1".fromStringUnsafe, WeightedEdge("G1".fromStringUnsafe, 60))

      graph.edges should be(List("M1".fromStringUnsafe -> "G1".fromStringUnsafe))
    }
  }

  it should "correctly list neighbours of a node" in {
    forSingleInstance(emptyGraphGen) { wg =>
      val graph = wg
        .addEdge("M1".fromStringUnsafe, WeightedEdge("G1".fromStringUnsafe, 60))

      graph.neighbours("M1".fromStringUnsafe) should be(List("G1".fromStringUnsafe))
    }
  }

  it should "correctly list vertices in a graph" in {
    forSingleInstance(emptyGraphGen) { wg =>
      val graph = wg
        .addEdge("M1".fromStringUnsafe, WeightedEdge("G1".fromStringUnsafe, 60))
        .addEdge("G1".fromStringUnsafe, WeightedEdge("C2".fromStringUnsafe, 20))
        .addEdge("C2".fromStringUnsafe, WeightedEdge("C1".fromStringUnsafe, 40))

      graph.vertices should contain theSameElementsAs List(
        "M1".fromStringUnsafe,
        "G1".fromStringUnsafe,
        "C2".fromStringUnsafe
      )
    }
  }

  it should "correctly list all nodes in a graph" in {
    forSingleInstance(emptyGraphGen) { wg =>
      val graph = wg
        .addEdge("M1".fromStringUnsafe, WeightedEdge("G1".fromStringUnsafe, 60))
        .addEdge("G1".fromStringUnsafe, WeightedEdge("C2".fromStringUnsafe, 20))
        .addEdge("C2".fromStringUnsafe, WeightedEdge("C1".fromStringUnsafe, 40))

      graph.nodes should contain theSameElementsAs List(
        "M1".fromStringUnsafe,
        "G1".fromStringUnsafe,
        "C2".fromStringUnsafe,
        "C1".fromStringUnsafe
      )
    }
  }

}
