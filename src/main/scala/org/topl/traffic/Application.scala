package org.topl.traffic

import scala.io
import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, ExitCode, IO, IOApp, Resource, Sync}
import org.http4s.blaze.client.BlazeClientBuilder
import org.topl.traffic.cli.Cli
import org.topl.traffic.protocol.models.State
import org.topl.traffic.settings.AppSettings
import tofu.syntax.monadic._

import pureconfig.generic.auto._
import tofu.fs2Instances._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

object Application extends IOApp {
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  override def run(args: List[String]): IO[ExitCode] =
    resources[IO](args.headOption).use { case (client, getFile, appState, settings) =>
      Cli.run[IO](client, IO.pure(getFile), appState, settings).as(ExitCode.Success)
    }

  private def resources[F[_]: ConcurrentEffect: Sync](configPathOpt: Option[String]) =
    for {
      client           <- BlazeClientBuilder[F](ec).resource
      getFile          <- Resource.eval(((path: String) => io.Source.fromFile(path)).pure)
      applicationState <- Resource.eval(Ref[F].of(State()))
      settings         <- Resource.eval(AppSettings.load(configPathOpt))
    } yield (client, getFile, applicationState, settings)
}
