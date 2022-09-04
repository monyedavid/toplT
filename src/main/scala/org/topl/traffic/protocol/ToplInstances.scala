package org.topl.traffic.protocol

import fs2.Stream
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import org.topl.traffic.protocol.models.TrafficData
import tofu.streams.Compile

object ToplInstances {

  implicit val TrafficDataEncoder: Encoder[TrafficData] = deriveEncoder[TrafficData]
  implicit val TrafficDataDecoder: Decoder[TrafficData] = deriveDecoder[TrafficData]

}
