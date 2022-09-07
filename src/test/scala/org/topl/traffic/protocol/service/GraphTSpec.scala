package org.topl.traffic.protocol.service

import cats.Monad
import cats.effect.{IO, Resource}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.scalatest.{PrivateMethodTester, TryValues}
import org.topl.traffic.commonGenerators.forSingleInstance
import org.topl.traffic.protocol.models.{Intersection, Point, WeightedEdge, WeightedGraph}
import org.topl.traffic.protocol.models.generators.trafficDataConstGen
import org.topl.traffic.settings.ServiceSettings
import tofu.syntax.monadic._
import tofu.fs2Instances._

class GraphTSpec extends AnyFlatSpec with should.Matchers with TryValues with PrivateMethodTester {
  import org.topl.traffic.protocol.service.GraphTSpec._
  "GraphT" should "correctly transform TrafficData into Graph[InterSection]" in {
    withResources[IO]
      .use { s =>
        forSingleInstance(trafficDataConstGen) { td =>
          val graph = GraphT[IO].mkGraph(td, s).unsafeRunSync()
          val expectedR = WeightedGraph(
            Map(
              Intersection("A", "1") -> List(WeightedEdge(Intersection("B", "1"), 28.0)),
              Intersection("A", "2") -> List(
                WeightedEdge(Intersection("B", "2"), 50.0),
                WeightedEdge(Intersection("A", "1"), 59.0)
              )
            )
          )
          val expectedRE = List(
            (Intersection("A", "1"), Intersection("B", "1")),
            (Intersection("A", "2"), Intersection("B", "2")),
            (Intersection("A", "2"), Intersection("A", "1"))
          )

          graph should be(expectedR)
          graph.edges should be(expectedRE)

        }
        IO.unit
      }
      .unsafeRunSync()
  }

  it should "correctly collect point's transit times" in {
    val collectPointTransitTimes = PrivateMethod[IO[List[(Point, List[Double])]]]('collectPointTransitTimes)
    withResources[IO]
      .use { case ServiceSettings(chunkSize, _) =>
        forSingleInstance(trafficDataConstGen) { td =>
          val gT  = GraphT[IO]
          val ptt = (gT invokePrivate collectPointTransitTimes(td.trafficMeasurements, chunkSize)).unsafeRunSync()

          val expectedR = List(
            (Point(Intersection("A", "1"), Intersection("B", "1")), List(28.0, 28.0, 28.0)),
            (Point(Intersection("A", "2"), Intersection("A", "1")), List(59.0, 59.0, 59.0)),
            (Point(Intersection("A", "2"), Intersection("B", "2")), List(50.0, 50.0, 50.0))
          )

          ptt should be(expectedR)
        }
        IO.unit
      }
      .unsafeRunSync()
  }

  it should "correctly return average measurement" in {
    val avgMeasurements = PrivateMethod[IO[List[(Point, Double)]]]('avgMeasurements)
    withResources[IO]
      .use { case ServiceSettings(chunkSize, _) =>
        forSingleInstance(trafficDataConstGen) { td =>
          val gT       = GraphT[IO]
          val averages = (gT invokePrivate avgMeasurements(td.trafficMeasurements, chunkSize)).unsafeRunSync()
          val expectedR = List(
            (Point(Intersection("A", "1"), Intersection("B", "1")), 28.0),
            (Point(Intersection("A", "2"), Intersection("A", "1")), 59.0),
            (Point(Intersection("A", "2"), Intersection("B", "2")), 50.0)
          )

          averages should be(expectedR)

        }
        IO.unit
      }
      .unsafeRunSync()
  }
}

object GraphTSpec {

  import tofu.syntax.monadic._

  def withResources[F[_]: Monad] =
    for {
      settings <- Resource.eval(ServiceSettings(chunkSize = 10, pathChunkSize = 1).pure[F])
    } yield settings
}
