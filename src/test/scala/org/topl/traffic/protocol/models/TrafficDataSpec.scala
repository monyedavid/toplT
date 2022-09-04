package org.topl.traffic.protocol.models

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.topl.traffic.commonGenerators._
import org.topl.traffic.protocol.models.generators._

class TrafficDataSpec extends AnyFlatSpec with should.Matchers {
  "TrafficData" should "generate models from json" in {
    forSingleInstance(trafficDataJsonSample) { json =>
      TrafficData.fromJson(json) should be(
        Right(
          TrafficData(trafficMeasurements =
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
        )
      )
    }
  }
}
