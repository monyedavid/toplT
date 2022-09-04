package org.topl.traffic.protocol.reader

import org.topl.traffic.protocol.models.{ExtJsonSource, FileJsonSource, JsonSource, TrafficData}
import cats.effect.ConcurrentEffect
import cats.MonadError
import io.circe.{parser, Json}
import org.http4s.client.Client

import scala.io.BufferedSource
import tofu.syntax.monadic._
import org.http4s.{Method, Request, Status, Uri}
import org.http4s.circe.CirceEntityDecoder._

trait JsonReader[F[_], G[_]] {
  def read: F[G[TrafficData]]
}

object JsonReader {

  def apply[F[_]: ConcurrentEffect, G[_]](source: JsonSource, client: Client[F], getFile: F[String => BufferedSource])(
    implicit G: MonadError[G, Throwable]
  ): JsonReader[F, G] = new JsonReader[F, G] {

    private def readFromExt(uri: String): F[G[TrafficData]] =
      client.run(makeGetRequest(uri)).use {
        case Status.Successful(r) =>
          r.attemptAs[TrafficData]
            .leftMap(_.message)
            .value
            .map({
              case Left(error)  => G.raiseError(new Exception(s"Request failed with error: $error"))
              case Right(value) => G.pure(value)
            })

        case r =>
          r.as[String].map(b => G.raiseError(new Exception(s"Request failed with status ${r.status.code} and body $b")))
      }

    private def readFromFile(filePath: String): F[G[TrafficData]] =
      getFile.map { f =>
        val jsonContent = f(filePath).getLines().mkString("")
        val parsedJson  = parser.parse(jsonContent).getOrElse(Json.Null)
        TrafficData.fromJson(parsedJson) match {
          case Left(value)  => G.raiseError(new Exception(value.message))
          case Right(value) => G.pure(value)
        }
      }

    private def makeGetRequest(uri: String) =
      Request[F](Method.GET, Uri.unsafeFromString(uri))

    def read: F[G[TrafficData]] =
      source match {
        case ExtJsonSource(uri)       => readFromExt(uri)
        case FileJsonSource(filePath) => readFromFile(filePath)
      }
  }
}
