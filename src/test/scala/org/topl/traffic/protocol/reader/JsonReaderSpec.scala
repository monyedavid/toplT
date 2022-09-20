package org.topl.traffic.protocol.reader

import cats.effect.{ConcurrentEffect, ContextShift, IO, Resource, Timer}
import org.http4s.blaze.client.BlazeClientBuilder
import org.scalatest.TryValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.topl.traffic.protocol.reader.JsonReaderSpec.{withResources, withServerResources}
import org.topl.traffic.utils.TestServer
import tofu.syntax.monadic._

import scala.io
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.util.{Success, Try}
import org.topl.traffic.protocol.models.{ExtJsonSource, FileJsonSource, JsonSource}

class JsonReaderSpec extends AnyFlatSpec with should.Matchers with TryValues {

  implicit val ec: ExecutionContext          = ExecutionContext.global
  implicit val ece: ExecutionContextExecutor = ExecutionContext.global
  implicit val cs: ContextShift[IO]          = IO.contextShift(ec)
  implicit val timer: Timer[IO]              = IO.timer(ec)
  val testServerUrl                          = "http://localhost:3000"
  val testFilePath                           = "/Users/bot/Desktop/void/com.github/toplT/src/test/resources/test-sample-data.json"

  "Json reader" should "read data correctly from files" in {
    withResources[IO](ec)
      .use { case (c, getFile) =>
        val jsonSource: JsonSource = FileJsonSource(testFilePath)
        val reader                 = JsonReader[IO, Try](jsonSource, c, IO.pure(getFile))
        val o                      = reader.read.unsafeRunSync()

        o.isSuccess should be(true)
        o should be(Success(TestServer.td))

        IO.unit
      }
      .unsafeRunSync()
  }

  it should "read data correctly from external sources(API)" in {
    withServerResources[IO](ec, ece)
      .use { case (client, getFile, _) =>
        val jsonSource: JsonSource = ExtJsonSource(testServerUrl + "/p")
        val reader                 = JsonReader[IO, Try](jsonSource, client, IO.pure(getFile))
        val o                      = reader.read.unsafeRunSync()

        o.isSuccess should be(true)
        o should be(Success(TestServer.td))
        IO.unit
      }
      .unsafeRunSync()
  }

  it should "fail on incorrect response from external sources(API)" in {
    withServerResources[IO](ec, ece)
      .use { case (client, getFile, _) =>
        val jsonSource: JsonSource = ExtJsonSource(testServerUrl + "/f")
        val reader                 = JsonReader[IO, Try](jsonSource, client, IO.pure(getFile))
        val o                      = reader.read.unsafeRunSync()

        o.isFailure should be(true)
        IO.unit
      }
      .unsafeRunSync()
  }
}

object JsonReaderSpec {

  import cats.effect.Sync

  def withResources[F[_]: ConcurrentEffect: Timer: Sync](ec: ExecutionContext) =
    for {
      client  <- BlazeClientBuilder[F](ec).resource
      getFile <- Resource.eval(((path: String) => io.Source.fromFile(path)).pure)
    } yield (client, getFile)

  def withServerResources[F[_]: ConcurrentEffect: Timer: Sync](ec: ExecutionContext, ece: ExecutionContextExecutor) =
    for {
      client  <- BlazeClientBuilder[F](ec).resource
      getFile <- Resource.eval(((path: String) => io.Source.fromFile(path)).pure)
      server  <- TestServer(ece).resource
    } yield (client, getFile, server)

}
