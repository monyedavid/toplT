package org.topl.traffic.protocol.models

import derevo.circe.{decoder, encoder}
import derevo.derive

@derive(encoder, decoder)
final case class TrafficMeasurements(measurementTime: Long, measurements: List[Measurement])
