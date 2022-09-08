package org.topl.traffic.protocol.models

import io.circe.Json
import org.scalacheck.Gen
import Intersection._

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

  def testGraphGen: Gen[WeightedGraph[Intersection]] = Gen.const(
    Graph[Intersection]()
      .addEdge("M1".fromStringUnsafe, WeightedEdge("G1".fromStringUnsafe, 60))
      .addEdge("M1".fromStringUnsafe, WeightedEdge("C1".fromStringUnsafe, 160))
      .addEdge("M1".fromStringUnsafe, WeightedEdge("D1".fromStringUnsafe, 170))
      .addEdge("M1".fromStringUnsafe, WeightedEdge("J1".fromStringUnsafe, 50))
      .addEdge("M1".fromStringUnsafe, WeightedEdge("B1".fromStringUnsafe, 250))
      .addEdge("M1".fromStringUnsafe, WeightedEdge("D2".fromStringUnsafe, 360))
      .addEdge("G1".fromStringUnsafe, WeightedEdge("C2".fromStringUnsafe, 20))
      .addEdge("C2".fromStringUnsafe, WeightedEdge("C1".fromStringUnsafe, 40))
      .addEdge("C1".fromStringUnsafe, WeightedEdge("C1".fromStringUnsafe, 210))
      .addEdge("D1".fromStringUnsafe, WeightedEdge("S1".fromStringUnsafe, 320))
      .addEdge("S1".fromStringUnsafe, WeightedEdge("B1".fromStringUnsafe, 210))
      .addEdge("B1".fromStringUnsafe, WeightedEdge("S1".fromStringUnsafe, 260))
      .addEdge("B1".fromStringUnsafe, WeightedEdge("D2".fromStringUnsafe, 90))
      .addEdge("D2".fromStringUnsafe, WeightedEdge("B1".fromStringUnsafe, 140))
      .addEdge("J1".fromStringUnsafe, WeightedEdge("D1".fromStringUnsafe, 110))
      .addEdge("J1".fromStringUnsafe, WeightedEdge("K2".fromStringUnsafe, 190))
      .addEdge("K2".fromStringUnsafe, WeightedEdge("J1".fromStringUnsafe, 160))
      .addEdge("K2".fromStringUnsafe, WeightedEdge("D2".fromStringUnsafe, 90))
  )

  def emptyGraphGen: Gen[WeightedGraph[Intersection]] = Gen.const(Graph[Intersection]())

}
