package org.topl.traffic.protocol.models

final case class Intersection(avenue: String, street: String)

object Intersection {

  def empty: Intersection = Intersection("", "")

  implicit class RichInterSection(s: String) {
    def fromStringUnsafe: Intersection = Intersection(s(0).toString, s(1).toString)
  }

}
