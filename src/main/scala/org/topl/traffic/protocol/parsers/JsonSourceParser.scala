package org.topl.traffic.protocol.parsers

import cats.MonadError
import org.topl.traffic.protocol.models._
import org.topl.traffic.protocol.parsers.alegbra.Parser

object JsonSourceParser {

  private def isHttp(s: String): Boolean = s.matches("^(http|https|ftp)://.*$")

  private def isFilePath(s: String): Boolean =
    s.matches("^(?:[\\w]\\:|\\\\)(\\\\[a-z_\\-\\s0-9\\.]+)+\\.(json)$") ||
    s.matches("^(?:[\\w]\\:|\\/)(\\/[a-z_\\-\\s0-9\\.]+)+\\.(json)$") ||
    s.matches("^(\\\\[a-z_\\-\\s0-9\\.]+)+\\.(json)$") ||
    s.matches("^(\\/[a-z_\\-\\s0-9\\.]+)+\\.(json)$")

  def apply[G[_]](implicit G: MonadError[G, Throwable]): Parser[G, JsonSource] = {
    case s if isHttp(s)     => G.pure(ExtJsonSource(s))
    case s if isFilePath(s) => G.pure(FileJsonSource(s))
    case _                  => G.raiseError(new Exception("invalid source bro"))
  }
}
