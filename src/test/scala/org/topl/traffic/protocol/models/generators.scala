package org.topl.traffic.protocol.models

import io.circe.Json
import org.scalacheck.Gen

object generators {

  def measurementGen: Gen[Measurement] =
    for {
      startAvenue <- Gen.alphaLowerStr
      startStreet <- Gen.alphaLowerStr
      transitTime <- Gen.posNum[Double]
      endAvenue   <- Gen.alphaLowerStr
      endStreet   <- Gen.alphaLowerStr
    } yield Measurement(startAvenue, startStreet, transitTime, endAvenue, endStreet)

  def trafficMeasurementGen(length: Int): Gen[TrafficMeasurements] =
    for {
      measurements    <- Gen.listOfN(length, measurementGen)
      measurementTime <- Gen.posNum[Long]
    } yield TrafficMeasurements(measurementTime, measurements)

  def trafficDataGen(length: Int): Gen[TrafficData] =
    for {
      trafficMeasurements <- Gen.listOfN(length, trafficMeasurementGen(length))
    } yield TrafficData(trafficMeasurements)

  def trafficDataConstGen: Gen[TrafficData] = Gen.const(
    TrafficData(trafficMeasurements =
      List(
        TrafficMeasurements(
          measurementTime = 86544,
          measurements = List(
            Measurement(
              startAvenue = "A",
              startStreet = "1",
              transitTime = 28,
              endAvenue   = "B",
              endStreet   = "1"
            ),
            Measurement(
              startAvenue = "A",
              startStreet = "2",
              transitTime = 59,
              endAvenue   = "A",
              endStreet   = "1"
            ),
            Measurement(
              startAvenue = "A",
              startStreet = "2",
              transitTime = 50,
              endAvenue   = "B",
              endStreet   = "2"
            )
          )
        ),
        TrafficMeasurements(
          measurementTime = 86544,
          measurements = List(
            Measurement(
              startAvenue = "A",
              startStreet = "1",
              transitTime = 28,
              endAvenue   = "B",
              endStreet   = "1"
            ),
            Measurement(
              startAvenue = "A",
              startStreet = "2",
              transitTime = 59,
              endAvenue   = "A",
              endStreet   = "1"
            ),
            Measurement(
              startAvenue = "A",
              startStreet = "2",
              transitTime = 50,
              endAvenue   = "B",
              endStreet   = "2"
            )
          )
        ),
        TrafficMeasurements(
          measurementTime = 86544,
          measurements = List(
            Measurement(
              startAvenue = "A",
              startStreet = "1",
              transitTime = 28,
              endAvenue   = "B",
              endStreet   = "1"
            ),
            Measurement(
              startAvenue = "A",
              startStreet = "2",
              transitTime = 59,
              endAvenue   = "A",
              endStreet   = "1"
            ),
            Measurement(
              startAvenue = "A",
              startStreet = "2",
              transitTime = 50,
              endAvenue   = "B",
              endStreet   = "2"
            )
          )
        )
      )
    )
  )

  def trafficDataJsonSampleGen: Gen[Json] =
    Gen.const(
      Json.obj(
        "trafficMeasurements" -> Json.arr(
          Json.obj(
            "measurementTime" -> Json.fromString("86544"),
            "measurements" -> Json.arr(
              Json.obj(
                "startAvenue" -> Json.fromString("A"),
                "startStreet" -> Json.fromString("1"),
                "transitTime" -> Json.fromString("28.000987663134676"),
                "endAvenue"   -> Json.fromString("B"),
                "endStreet"   -> Json.fromString("1")
              ),
              Json.obj(
                "startAvenue" -> Json.fromString("A"),
                "startStreet" -> Json.fromString("2"),
                "transitTime" -> Json.fromString("59.71131185379898"),
                "endAvenue"   -> Json.fromString("A"),
                "endStreet"   -> Json.fromString("1")
              ),
              Json.obj(
                "startAvenue" -> Json.fromString("A"),
                "startStreet" -> Json.fromString("2"),
                "transitTime" -> Json.fromString("50.605942255619624"),
                "endAvenue"   -> Json.fromString("B"),
                "endStreet"   -> Json.fromString("2")
              )
            )
          )
        )
      )
    )

}
