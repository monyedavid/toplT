package org.topl.traffic.utils

import cats.Monad
import cats.effect.{ConcurrentEffect, Timer}
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.dsl.Http4sDsl
import org.topl.traffic.protocol.models.{Measurement, TrafficData, TrafficMeasurements}
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder

import scala.concurrent.ExecutionContextExecutor

object TestServer {

  val td: TrafficData = TrafficData(trafficMeasurements =
    List(
      TrafficMeasurements(
        measurementTime = 86544,
        measurements = List(
          Measurement(
            startAvenue = "A",
            startStreet = "1",
            transitTime = 28.000987663134676,
            endAvenue   = "B",
            endStreet   = "1"
          ),
          Measurement(
            startAvenue = "A",
            startStreet = "2",
            transitTime = 59.71131185379898,
            endAvenue   = "A",
            endStreet   = "1"
          ),
          Measurement(
            startAvenue = "A",
            startStreet = "2",
            transitTime = 50.605942255619624,
            endAvenue   = "B",
            endStreet   = "2"
          )
        )
      )
    )
  )

  val m: Measurement = Measurement(
    startAvenue = "A",
    startStreet = "2",
    transitTime = 50.605942255619624,
    endAvenue   = "B",
    endStreet   = "2"
  )

  def api[F[_]: ConcurrentEffect: Monad]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "p" =>
        Ok(td.asJson)
      case GET -> Root / "f" =>
        Ok(m.asJson)
    }
  }

  def apply[F[_]: ConcurrentEffect: Timer](ec: ExecutionContextExecutor): BlazeServerBuilder[F] =
    BlazeServerBuilder[F](ec).bindHttp(3000, "localhost").withHttpApp(Router("/" -> api).orNotFound)
}
