package org.topl.traffic.algorithm

import cats.Monad
import cats.effect.{IO, Resource}
import org.scalatest.{PrivateMethodTester, TryValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.topl.traffic.algorithm.PathTSpec.withResources
import org.topl.traffic.commonGenerators.forSingleInstance
import org.topl.traffic.protocol.models.{Intersection, WeightedEdge}
import org.topl.traffic.protocol.models.generators.testGraphGen
import org.topl.traffic.settings.ServiceSettings

class PathTSpec extends AnyFlatSpec with should.Matchers with TryValues with PrivateMethodTester {
  "PathT" should "correctly generate shortest path from source node to given nodes via private method 'extractSPathsTRec' " in {
    import Intersection._
    forSingleInstance(testGraphGen) { tG =>
      val sourceNode = "M1".fromStringUnsafe
      val dijkstra   = new Dijkstra(tG, sourceNode)

      val ss = dijkstra.shortestPathTRec(
        ShortStep(unProcessed = tG.vertices.toSet, transitTimes = dijkstra.sTransitTimes)
      )

      tG.vertices.map(node =>
        PathT.extractSPathsTRec("M1".fromStringUnsafe, Intersection.empty, node, ss.parents)
      ) should contain theSameElementsAs
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

  it should "handle missing paths" in {
    import Intersection._
    forSingleInstance(testGraphGen) { tG =>
      val sourceNode    = "M1".fromStringUnsafe
      val isolatedNode  = "I1".fromStringUnsafe
      val isolatedNode2 = "I2".fromStringUnsafe
      val dijkstra      = new Dijkstra(tG.addEdge(isolatedNode, WeightedEdge(isolatedNode2, 1)), sourceNode)

      val ss = dijkstra.shortestPathTRec(
        ShortStep(unProcessed = tG.vertices.toSet, transitTimes = dijkstra.sTransitTimes)
      )

      PathT.extractSPathsTRec("M1".fromStringUnsafe, Intersection.empty, isolatedNode, ss.parents) should be(List())

    }
  }

  it should "handle cyclic errors" in {
    import Intersection._
    forSingleInstance(testGraphGen) { tG =>
      val sourceNode    = "M1".fromStringUnsafe
      val isolatedNode  = "I1".fromStringUnsafe
      val isolatedNode2 = "I2".fromStringUnsafe
      val dijkstra = new Dijkstra(
        tG.addEdge(isolatedNode, WeightedEdge(isolatedNode2, 1))
          .addEdge(isolatedNode2, WeightedEdge(isolatedNode, 1)),
        sourceNode
      )

      val ss = dijkstra.shortestPathTRec(
        ShortStep(unProcessed = tG.vertices.toSet, transitTimes = dijkstra.sTransitTimes)
      )

      PathT.extractSPathsTRec("M1".fromStringUnsafe, Intersection.empty, isolatedNode, ss.parents) should be(List())
      PathT.extractSPathsTRec("M1".fromStringUnsafe, Intersection.empty, isolatedNode2, ss.parents) should be(List())

    }
  }

  it should "correctly collect shortStep for a source - node" in {
    withResources[IO]
      .use { s =>
        forSingleInstance(testGraphGen) { tG => }
        IO.unit
      }
      .unsafeRunSync()
  }

}

object PathTSpec {
  import tofu.syntax.monadic._

  def withResources[F[_]: Monad] =
    for {
      settings <- Resource.eval(ServiceSettings(chunkSize = 10, pathChunkSize = 6).pure[F])
    } yield settings
}
