package org.topl.traffic.cli

import cats.Monad
import cats.data.EitherT
import cats.effect.ConcurrentEffect
import cats.effect.concurrent.Ref
import org.http4s.client.Client
import org.topl.traffic.algorithm.PathT
import org.topl.traffic.protocol.parsers.{JsonSourceParser, PointParser}
import org.topl.traffic.protocol.models.{Intersection, Result, State}
import org.topl.traffic.protocol.reader.JsonReader
import org.topl.traffic.protocol.service.GraphT
import org.topl.traffic.settings.{AppSettings, ServiceSettings}
import org.topl.traffic.streaming.CompileStream

import scala.io.BufferedSource
import tofu.syntax.monadic._

object Execute {

  def executeWithExistingSource[F[_]: Monad](
    applicationState: Ref[F, State],
    pointS: String
  ): EitherT[F, Throwable, Result[Intersection]] = {
    val pp = PointParser[Either[Throwable, *]]
    for {
      state <- EitherT.right(applicationState.get)
      point <- EitherT.fromEither(pp.fromString(pointS))
    } yield state.getShortestPath(point)
  }

  def executeWithNewSource[F[_]: Monad: ConcurrentEffect: CompileStream](
    applicationState: Ref[F, State],
    pointS: String,
    sourceS: String,
    client: Client[F],
    getFile: F[String => BufferedSource],
    settings: ServiceSettings
  ): EitherT[F, Throwable, Result[Intersection]] =
    for {
      point         <- EitherT.fromEither(PointParser[Either[Throwable, *]].fromString(pointS))
      jSource       <- EitherT.fromEither(JsonSourceParser[Either[Throwable, *]].fromString(sourceS))
      trafficData   <- EitherT(JsonReader[F, Either[Throwable, *]](jSource, client, getFile).read)
      weightedGraph <- EitherT.right(GraphT[F].mkGraph(trafficData, settings))
      shortestPaths <- EitherT.right(PathT[F].findShortestPath(weightedGraph, settings))
      _ = applicationState.update(_ => State(Some(sourceS), shortestPaths)) // update state :)
      state <- EitherT.right(applicationState.get)
    } yield state.getShortestPath(point)

  def executeCommands[F[_]: ConcurrentEffect: Monad: CompileStream](
    command: Command,
    client: Client[F],
    getFile: F[String => BufferedSource],
    applicationState: Ref[F, State],
    settings: AppSettings
  ): F[Unit] = command match {
    case Invalid => handleExecution(EitherT.leftT(new Exception("Invalid parameters given")))
    case WithDefaultSource(i1, i2) =>
      handleExecution(
        executeWithNewSource(
          applicationState,
          i1 + " " + i2,
          settings.default.jsonPath,
          client,
          getFile,
          settings.service
        )
      )
    case WithExistingSource(i1, i2) => handleExecution(executeWithExistingSource(applicationState, i1 + " " + i2))
    case WithNewSource(src, i1, i2) =>
      handleExecution(executeWithNewSource(applicationState, i1 + " " + i2, src, client, getFile, settings.service))
  }

  // print to terminal error/shortestPath :)
  def handleExecution[F[_]: Monad](result: EitherT[F, Throwable, Result[Intersection]]): F[Unit] =
    result.value.map {
      case Left(throwable) => println(s"Error: $throwable")
      case Right(r)        => println(r)
    }

}

// commandT:
// newSource => JsonSource => Stream[Average] => Djkistra Algorithm
// defaultSource => JsonSource.default(bufferedSource) => Stream[Average] => Djkistra Algorithm
// existing => parse line => Point
//                applicationState.getShortestPath(Point) => path
