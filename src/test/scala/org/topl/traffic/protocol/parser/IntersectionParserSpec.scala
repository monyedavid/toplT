package org.topl.traffic.protocol.parser

import org.scalatest.TryValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.topl.traffic.protocol.parsers.IntersectionParser

import scala.util.{Success, Try}

class IntersectionParserSpec extends AnyFlatSpec with should.Matchers with TryValues {

  import org.topl.traffic.protocol.models._

  "Intersection parser" should "correctly parse intersections from string" in {
    val ip = IntersectionParser[Try]

    ip.fromString("A1") should be(Success(Intersection(avenue = "A", street = "1")))
    ip.fromString("Z1") should be(Success(Intersection(avenue = "Z", street = "1")))
    ip.fromString("T19") should be(Success(Intersection(avenue = "T", street = "19")))
  }

  it should "fail on wrong string format" in {
    val ip = IntersectionParser[Try]

    ip.fromString("A 1").isFailure should be(true)
    ip.fromString("AC1").isFailure should be(true)
    ip.fromString("").isFailure should be(true)
  }

}
