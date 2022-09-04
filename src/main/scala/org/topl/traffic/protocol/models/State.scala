package org.topl.traffic.protocol.models

final case class State(
  jsonSource: Option[String]                    = None,
  shortestPaths: Map[Point, List[Intersection]] = Map()
) {
  def getShortestPath(point: Point): List[Intersection] = shortestPaths.getOrElse(point, List())
}
