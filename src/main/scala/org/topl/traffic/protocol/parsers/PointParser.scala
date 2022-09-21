package org.topl.traffic.protocol.parsers

import cats.MonadError
import org.topl.traffic.protocol.models.Point
import org.topl.traffic.protocol.parsers.alegbra.Parser
import tofu.syntax.monadic._

object PointParser {

  // A1 A2
  def apply[G[_]](implicit G: MonadError[G, Throwable]): Parser[G, Point] =
    (s: String) => {
      val pointsS = s.trim.split(" ")
      val iP      = IntersectionParser[G]
      if (pointsS.length == 2) {
        (iP.fromString(pointsS(0)), iP.fromString(pointsS(1))).mapN(Point)
      } else G.raiseError(new Exception(s"incorrect argument giving for point: $s"))
    }
}
