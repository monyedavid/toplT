package org.topl.traffic.protocol.parsers

import cats.MonadError
import org.topl.traffic.protocol.models.Intersection
import org.topl.traffic.protocol.parsers.alegbra.Parser

import scala.util.Try

object IntersectionParser {

  // A1 => Avenue A; Street 1
  def apply[G[_]](implicit G: MonadError[G, Throwable]): Parser[G, Intersection] =
    (s: String) => {

      val iOp = for {
        avenue <- s.headOption
        street <- Try(s.tail.toInt).toOption
      } yield Intersection(avenue.toString, street.toString)

      iOp match {
        case Some(intersection) => G.pure(intersection)
        case None               => G.raiseError(new Exception(s"incorrect argument giving for an intersection: $s"))
      }

    }

}
