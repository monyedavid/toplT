package org.topl.traffic.protocol.models

import derevo.circe.{decoder, encoder}
import derevo.derive

@derive(encoder, decoder)
final case class Measurement(
  startAvenue: String,
  startStreet: String,
  transitTime: Double,
  endAvenue: String,
  endStreet: String
)

object Measurement {

  // source(start), destination(end)
  def toPoint(m: Measurement): Point =
    Point(Intersection(m.startAvenue, m.startStreet), Intersection(m.endAvenue, m.endStreet))

  def asTuple(m: Measurement): (Point, Double) = (toPoint(m), m.transitTime)

}
