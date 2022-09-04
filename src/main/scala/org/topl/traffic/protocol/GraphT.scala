package org.topl.traffic.protocol

import cats.Monad
import fs2.{Chunk, Pipe, Stream}
import org.topl.traffic.protocol.models.{
  Graph,
  Intersection,
  Measurement,
  Point,
  TrafficData,
  TrafficMeasurements,
  WeightedEdge,
  WeightedGraph
}
import org.topl.traffic.settings.ServiceSettings
import org.topl.traffic.streaming.CompileStream
import tofu.syntax.monadic._
import tofu.syntax.streams.compile._

object GraphT {

  def toGraph[F[_]: Monad: CompileStream](
    trafficData: TrafficData,
    settings: ServiceSettings
  ): F[WeightedGraph[Intersection]] =
    for {
      avgM <- avgMeasurements(trafficData.trafficMeasurements, settings.chunkSize)
    } yield avgM.foldLeft(Graph[Intersection]()) { case (graph, (point, avgT)) =>
      graph.addEdge(point.source, WeightedEdge(point.destination, avgT))
    }

  // return List of avg transitTime measurements => Convert to Graph[Intersection]
  def avgMeasurements[F[_]: Monad: CompileStream](
    trafficMeasurements: List[TrafficMeasurements],
    chunkSize: Int
  ): F[List[(Point, Double)]] =
    for {
      ptt <- collectPointTransitTimes(trafficMeasurements, chunkSize)
      o <- Stream
             .emits[F, (Point, List[Double])](ptt)
             .chunkN(chunkSize)
             .through(asAvgS)
             .to[List]
    } yield o

  def collectPointTransitTimes[F[_]: Monad: CompileStream](
    trafficMeasurements: List[TrafficMeasurements],
    chunkSize: Int
  ): F[List[(Point, List[Double])]] =
    Stream
      .emits[F, TrafficMeasurements](trafficMeasurements)
      .chunkN(chunkSize)
      .through(asTupleS)
      .fold(Map[Point, List[Double]]()) { (m, t) =>
        val p = m.getOrElse(t._1, List.empty)
        m + (t._1 -> (t._2 :: p))
      }
      .map(_.toList)
      .to[List]
      .map(_.flatten)

  def asTupleS[F[_]: Monad: CompileStream]: Pipe[F, Chunk[TrafficMeasurements], (Point, Double)] =
    for {
      chunk <- _
      chunkL = chunk.map(_.measurements).toList.flatten
      o <- Stream.emits(chunkL.map(Measurement.asTuple))
    } yield o

  def asAvgS[F[_]: Monad: CompileStream]: Pipe[F, Chunk[(Point, List[Double])], (Point, Double)] =
    for {
      chunk <- _
      chunkL = chunk.map { case (p, l) => (p, l.sum / l.length) }.toList
      o <- Stream.emits(chunkL)
    } yield o

}
