package org.topl.traffic.protocol.parser

import org.scalatest.TryValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.topl.traffic.protocol.parsers.PointParser

import scala.util.{Success, Try}

class PointParserSpec extends AnyFlatSpec with should.Matchers with TryValues {

  import org.topl.traffic.protocol.models._

  "Point parser" should "correctly parse intersections from string" in {
    val ip = PointParser[Try]

    ip.fromString("A1 A2") should be(
      Success(Point(Intersection(avenue = "A", street = "1"), Intersection(avenue = "A", street = "2")))
    )
  }

  it should "fail on wrong string format" in {
    val ip = PointParser[Try]

    ip.fromString("A A2").isFailure should be(true)
    ip.fromString("A1 A").isFailure should be(true)
    ip.fromString("").isFailure should be(true)
  }

}
