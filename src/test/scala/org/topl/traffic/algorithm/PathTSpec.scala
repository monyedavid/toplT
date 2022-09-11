package org.topl.traffic.algorithm

import cats.Monad
import cats.effect.{IO, Resource}
import org.scalatest.{PrivateMethodTester, TryValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.topl.traffic.algorithm.PathT.SourceNode
import org.topl.traffic.algorithm.PathTSpec.withResources
import org.topl.traffic.commonGenerators.forSingleInstance
import org.topl.traffic.protocol.models.{Intersection, Point, Result, WeightedEdge}
import org.topl.traffic.protocol.models.generators.testGraphGen
import org.topl.traffic.settings.ServiceSettings
import tofu.fs2Instances._

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

  it should "collect shortStep for every node in graph via stream" in {
    import Intersection._
    val pathT            = PathT[IO]
    val collectShortStep = PrivateMethod[IO[List[(SourceNode, ShortStep[Intersection])]]]('collectShortStep)
    withResources[IO]
      .use { case ServiceSettings(_, pathChunkSize) =>
        forSingleInstance(testGraphGen) { tG =>
          val sSteps = (pathT invokePrivate collectShortStep(tG, pathChunkSize)).unsafeRunSync()
          val m1Step = sSteps.find(p => p._1 == "M1".fromStringUnsafe).map(_._2).get

          m1Step.unProcessed should be(Set[Intersection]())
          m1Step.parents.toList should contain theSameElementsAs Map(
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
          m1Step.transitTimes.toList should contain theSameElementsAs Map[Intersection, Double](
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
        IO.unit
      }
      .unsafeRunSync()
  }

  it should "build shortest path from given start node, shortStep & destination nodes" in {
    import Intersection._
    val sourceNode       = "M1".fromStringUnsafe
    val pathT            = PathT[IO]
    val collectShortStep = PrivateMethod[IO[List[(SourceNode, ShortStep[Intersection])]]]('collectShortStep)
    val buildShortestPathFromStartNode =
      PrivateMethod[List[(Point, Result[Intersection])]]('buildShortestPathFromStartNode)
    withResources[IO]
      .use { case ServiceSettings(_, pathChunkSize) =>
        forSingleInstance(testGraphGen) { tG =>
          val sSteps = (pathT invokePrivate collectShortStep(tG, pathChunkSize)).unsafeRunSync()
          val m1Step = sSteps.find(p => p._1 == sourceNode).map(_._2).get

          val o = pathT invokePrivate buildShortestPathFromStartNode(m1Step, tG.vertices, sourceNode)

          o should contain theSameElementsAs List(
            (
              Point("M1".fromStringUnsafe, "G1".fromStringUnsafe),
              Result(List("M1".fromStringUnsafe, "G1".fromStringUnsafe), 60.0)
            ),
            (Point("M1".fromStringUnsafe, "M1".fromStringUnsafe), Result(List("M1".fromStringUnsafe), 0.0)),
            (
              Point("M1".fromStringUnsafe, "S1".fromStringUnsafe),
              Result(
                List("M1".fromStringUnsafe, "J1".fromStringUnsafe, "D1".fromStringUnsafe, "S1".fromStringUnsafe),
                690.0
              )
            ),
            (
              Point("M1".fromStringUnsafe, "B1".fromStringUnsafe),
              Result(List("M1".fromStringUnsafe, "B1".fromStringUnsafe), 250.0)
            ),
            (
              Point("M1".fromStringUnsafe, "J1".fromStringUnsafe),
              Result(List("M1".fromStringUnsafe, "J1".fromStringUnsafe), 50.0)
            ),
            (
              Point("M1".fromStringUnsafe, "K2".fromStringUnsafe),
              Result(List("M1".fromStringUnsafe, "J1".fromStringUnsafe, "K2".fromStringUnsafe), 290.0)
            ),
            (
              Point("M1".fromStringUnsafe, "C2".fromStringUnsafe),
              Result(List("M1".fromStringUnsafe, "G1".fromStringUnsafe, "C2".fromStringUnsafe), 140.0)
            ),
            (
              Point("M1".fromStringUnsafe, "C1".fromStringUnsafe),
              Result(
                List("M1".fromStringUnsafe, "G1".fromStringUnsafe, "C2".fromStringUnsafe, "C1".fromStringUnsafe),
                260.0
              )
            ),
            (
              Point("M1".fromStringUnsafe, "D1".fromStringUnsafe),
              Result(List("M1".fromStringUnsafe, "J1".fromStringUnsafe, "D1".fromStringUnsafe), 210.0)
            ),
            (
              Point("M1".fromStringUnsafe, "D2".fromStringUnsafe),
              Result(
                List("M1".fromStringUnsafe, "J1".fromStringUnsafe, "K2".fromStringUnsafe, "D2".fromStringUnsafe),
                620.0
              )
            )
          )

        }
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
