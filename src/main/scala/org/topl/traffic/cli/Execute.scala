package org.topl.traffic.cli

import cats.Monad
import cats.data.EitherT
import cats.effect.{ConcurrentEffect, Sync}
import cats.effect.concurrent.Ref
import org.http4s.client.Client
import org.topl.traffic.protocol.parsers.{JsonSourceParser, PointParser}
import org.topl.traffic.protocol.models.{Intersection, State}
import org.topl.traffic.protocol.reader.JsonReader
import org.topl.traffic.settings.AppSettings
import org.topl.traffic.streaming.CompileStream

import scala.io.BufferedSource
import scala.util.Try
import tofu.syntax.monadic._

object Execute {

  def executeWithExistingSource[F[_]: Monad](
    applicationState: Ref[F, State],
    pointS: String
  ): EitherT[F, Throwable, List[Intersection]] = {
    val pp = PointParser[Either[Throwable, *]]
    for {
      state <- EitherT.right(applicationState.get)
      point <- EitherT.fromEither(pp.fromString(pointS))
    } yield state.getShortestPath(point)
  }

  def executeWithNewSource[F[_]: Monad: ConcurrentEffect](
    applicationState: Ref[F, State],
    pointS: String,
    sourceS: String,
    client: Client[F],
    getFile: F[String => BufferedSource]
  ): EitherT[F, Throwable, List[Intersection]] =
    for {
      point       <- EitherT.fromEither(PointParser[Either[Throwable, *]].fromString(pointS))
      jSource     <- EitherT.fromEither(JsonSourceParser[Either[Throwable, *]].fromString(sourceS))
      trafficData <- EitherT(JsonReader[F, Either[Throwable, *]](jSource, client, getFile).read)
      // tData -> Graph[InterSection]
      // get vertices in graph:
      // for each vertex in vertices find shortest path to all nodes
      // update state :)
    } yield ???

  def executeCommands[F[_]: ConcurrentEffect: Monad: CompileStream](
    command: Command,
    client: Client[F],
    getFile: F[String => BufferedSource],
    applicationState: Ref[F, State],
    settings: AppSettings
  ): F[Unit] = command match {
    case Invalid => handleExecution(EitherT.leftT(new Exception("Invalid parameters given")))
    case WithDefaultSource(i1, i2) =>
      handleExecution(executeWithNewSource(applicationState, i1 + " " + i2, settings.default.jsonPath, client, getFile))
    case WithExistingSource(i1, i2) => handleExecution(executeWithExistingSource(applicationState, i1 + " " + i2))
    case WithNewSource(src, i1, i2) =>
      handleExecution(executeWithNewSource(applicationState, i1 + " " + i2, src, client, getFile))
  }

  // print to terminal error/shortestPath :)
  def handleExecution[F[_]: Monad](result: EitherT[F, Throwable, List[Intersection]]): F[Unit] = ???
}

// commandT:
// newSource => JsonSource => Stream[Average] => Djkistra Algorithm
// defaultSource => JsonSource.default(bufferedSource) => Stream[Average] => Djkistra Algorithm
// existing => parse line => Point
//                applicationState.getShortestPath(Point) => path
