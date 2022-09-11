package org.topl.traffic.cli

import cats.effect.ConcurrentEffect
import cats.effect.concurrent.Ref
import org.http4s.client.Client
import org.topl.traffic.cli.Execute.executeCommands
import org.topl.traffic.protocol.models.State
import org.topl.traffic.settings.AppSettings
import org.topl.traffic.streaming.CompileStream

import scala.io.BufferedSource
import tofu.syntax.monadic._

object Cli {

  def run[F[_]: ConcurrentEffect: CompileStream](
    client: Client[F],
    getFile: F[String => BufferedSource],
    applicationState: Ref[F, State],
    settings: AppSettings
  ): F[Unit] =
    for {
      line    <- scala.io.StdIn.readLine().split(" ").pure
      command <- Command(applicationState, line)
      _       <- executeCommands(command, client, getFile, applicationState, settings)
      _       <- run(client, getFile, applicationState, settings)
    } yield ()
}
