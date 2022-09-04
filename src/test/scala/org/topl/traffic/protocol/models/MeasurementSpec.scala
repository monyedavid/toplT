package org.topl.traffic.protocol.models

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import org.topl.traffic.commonGenerators._
import org.topl.traffic.protocol.models.generators._

class MeasurementSpec extends AnyFlatSpec with should.Matchers {
  "Measurement toInterSections" should "create source and destination intersection from measurement" in {
    forSingleInstance(measurementGen) { m =>
      Measurement.toPoint(m) should be(
        Point(Intersection(m.startAvenue, m.startStreet), Intersection(m.endAvenue, m.endStreet))
      )
    }
  }
}
