package org.topl.traffic.protocol.models

import derevo.circe.{decoder, encoder}
import derevo.derive
import io.circe.Decoder.Result
import io.circe.Json
/*
   trafficMeasurements:
      Array<{measurementTime: Long?,
            measurements: Array<{
                              startAvenue: String
                              startStreet: String
                              transitTime: Double
                              endAvenue: String
                              endStreet: String
                         }>
            }>
 */

@derive(encoder, decoder)
final case class TrafficData(trafficMeasurements: List[TrafficMeasurements])

// AVG-M => WGraph[Intersection] => Map[Point, List[Intersection]]

object TrafficData {

  def fromJson(j: Json): Result[TrafficData] = j.as[TrafficData]

}
