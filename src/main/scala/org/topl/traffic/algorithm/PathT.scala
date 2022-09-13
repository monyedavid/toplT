package org.topl.traffic.algorithm

import cats.Monad
import fs2.{Chunk, Pipe, Stream}
import org.topl.traffic.protocol.models.{Intersection, Point, Result, WeightedGraph}
import org.topl.traffic.settings.ServiceSettings
import org.topl.traffic.streaming.CompileStream
import tofu.syntax.monadic._
import tofu.syntax.streams.compile._

import scala.annotation.tailrec

// Transforms: Graph[InterSection] => shortest-path map for all vertices in graph
trait PathT[F[_], V, W] {
  def findShortestPath(graph: WeightedGraph[V], settings: ServiceSettings): F[Map[W, Result[V]]]
}

object PathT {

  type SourceNode = Intersection

  @tailrec
  final def extractSPathsTRec[V](
    primeSourceNode: V,
    previousParent: V,
    destinationNode: V,
    parents: Map[V, V],
    acc: List[V] = List()
  ): List[V] =
    if (destinationNode == primeSourceNode) primeSourceNode :: acc
    else {
      parents.get(destinationNode) match {
        case Some(node) =>
          if (previousParent == node) List.empty
          else extractSPathsTRec(primeSourceNode, destinationNode, node, parents, destinationNode :: acc)
        case None => List()
      }
    }

  def apply[F[_]: Monad: CompileStream]: PathT[F, Intersection, Point] = new PathT[F, Intersection, Point] {

    override def findShortestPath(
      graph: WeightedGraph[Intersection],
      settings: ServiceSettings
    ): F[Map[Point, Result[Intersection]]] =
      for {
        sSteps <- collectShortStep(graph, settings.pathChunkSize)
        sPaths <- buildShortestPaths(sSteps, graph.vertices, settings.pathChunkSize / 2)
      } yield sPaths

    private def collectShortStep(
      graph: WeightedGraph[Intersection],
      pathChunkSize: Int
    ): F[List[(SourceNode, ShortStep[Intersection])]] =
      Stream
        .emits[F, Intersection](graph.vertices)
        .chunkN(pathChunkSize)
        .through(mkShortStep(graph))
        .to[List]

    private def mkShortStep(
      graph: WeightedGraph[Intersection]
    ): Pipe[F, Chunk[SourceNode], (SourceNode, ShortStep[Intersection])] =
      for {
        chunk <- _
        chunkD = chunk.map(x => (x, Dijkstra(graph, x))).toList
        o <- Stream.emits(chunkD.map { case (s, d) =>
               val ss = d.shortestPathTRec(
                 ShortStep(unProcessed = graph.vertices.toSet, transitTimes = d.sTransitTimes)
               )
               (s, ss)
             })
      } yield o

    private def buildShortestPaths(
      sSteps: List[(SourceNode, ShortStep[Intersection])],
      nodes: List[Intersection],
      pathChunkSize: Int
    ): F[Map[Point, Result[Intersection]]] =
      Stream
        .emits(sSteps)
        .chunkN(pathChunkSize)
        .through(mkShortPath(nodes))
        .to[List]
        .map(_.toMap)

    private def mkShortPath(
      nodes: List[Intersection]
    ): Pipe[F, Chunk[(SourceNode, ShortStep[Intersection])], (Point, Result[Intersection])] =
      for {
        chunk <- _
        chunkL = chunk.toList
        o <- Stream.emits(chunkL.flatMap { case (sn, ss) => buildShortestPathFromStartNode(ss, nodes, sn) })
      } yield o

    private def buildShortestPathFromStartNode(
      shortStep: ShortStep[Intersection],
      nodes: List[Intersection],
      sourceNode: SourceNode
    ): List[(Point, Result[Intersection])] = {
      import cats.implicits._
      nodes.map { node =>
        val bestestPath = extractSPathsTRec(sourceNode, Intersection.empty, node, shortStep.parents)
        val bestestTime = bestestPath.map(shortStep.transitTimes.get).sequence.map(_.sum).getOrElse(0.0)
        (Point(sourceNode, node), Result(bestestPath, bestestTime))
      }
    }

  }
}

// Stream[F, Vertex] => new Dijkstra[V](graph, vertex) { => ShortStep[V], sourceNode }
// Stream[F, (ShortStep[V], sourceNode)] => Map[Point, List[Intersection]]
