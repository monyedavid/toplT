package org.topl.traffic.protocol.models

final case class State(
  jsonSource: Option[String]                      = None,
  shortestPaths: Map[Point, Result[Intersection]] = Map()
) {
  def getShortestPath(point: Point): Result[Intersection] = shortestPaths.getOrElse(point, Result.empty)
}
