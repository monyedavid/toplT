package org.topl.traffic.protocol.parsers

import cats.MonadError
import org.topl.traffic.protocol.models.Intersection
import org.topl.traffic.protocol.parsers.alegbra.Parser

object IntersectionParser {

  // A1 => Avenue A; Street 1
  def apply[G[_]](implicit G: MonadError[G, Throwable]): Parser[G, Intersection] =
    (s: String) => {
      val avS = s.split("")
      if (avS.length == 2) {
        G.pure(Intersection(avS(0), avS(1)))
      } else G.raiseError(new Exception("incorrect argument giving for an intersection"))
    }

}
