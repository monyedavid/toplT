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

  def trafficMeasurementGen: Gen[TrafficMeasurements] = ???
  def trafficDataGen: Gen[TrafficData]                = ???

  def trafficDataJsonSample: Gen[Json] =
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
