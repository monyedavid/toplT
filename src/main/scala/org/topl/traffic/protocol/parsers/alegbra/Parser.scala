package org.topl.traffic.protocol.parsers.alegbra

trait Parser[G[_], T] {
  def fromString(s: String): G[T]
}
