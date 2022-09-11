package org.topl.traffic.cli

import cats.effect.{IO, Resource, Sync}
import cats.effect.concurrent.Ref
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.topl.traffic.cli.CommandSpec.withResources

import org.topl.traffic.protocol.models.State

class CommandSpec extends AnyFlatSpec with should.Matchers {
  val resourcePath = "resource/path"
  val a1           = "A1"
  val a2           = "A2"

  "Command" should "correctly identify command with new source" in {
    withResources[IO]
      .use { stateRef =>
        val o = Command(stateRef, Array(resourcePath, a1, a2)).unsafeRunSync()

        o should be(WithNewSource(resourcePath, a1, a2))
        IO.unit
      }
      .unsafeRunSync()
  }

  it should "correctly identify command with default source" in {
    withResources[IO]
      .use { stateRef =>
        val o = Command(stateRef, Array(a1, a2)).unsafeRunSync()

        o should be(WithDefaultSource(a1, a2))
        IO.unit
      }
      .unsafeRunSync()
  }

  it should "correctly identify command with existing source" in {
    withResources[IO]
      .use { stateRef =>
        stateRef.update(_ => State(Some(resourcePath))).unsafeRunSync()
        val o = Command(stateRef, Array(a1, a2)).unsafeRunSync()

        o should be(WithExistingSource(a1, a2))
        IO.unit
      }
      .unsafeRunSync()
  }

  it should "correctly identify invalid line commands" in {
    withResources[IO]
      .use { stateRef =>
        stateRef.update(_ => State(Some(resourcePath))).unsafeRunSync()
        val o = Command(stateRef, Array()).unsafeRunSync()

        o should be(Invalid)
        IO.unit
      }
      .unsafeRunSync()
  }
}

object CommandSpec {

  def withResources[F[_]: Sync] =
    for {
      applicationState <- Resource.eval(Ref[F].of(State()))
    } yield applicationState
}
