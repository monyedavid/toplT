package org.topl.traffic.algorithm

import cats.Monad
import cats.effect.{IO, Resource}
import org.scalatest.{PrivateMethodTester, TryValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.topl.traffic.algorithm.PathTSpec.withResources
import org.topl.traffic.commonGenerators.forSingleInstance
import org.topl.traffic.protocol.models.generators.testGraphGen
import org.topl.traffic.settings.ServiceSettings

// PathT:
// from a weighted graph
// run dijkstra algorithm with every node as starting node

class PathTSpec extends AnyFlatSpec with should.Matchers with TryValues with PrivateMethodTester {
  "PathT" should "correctly transform a weighted graph into a HashMap of bestest Paths :)" in {}

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
