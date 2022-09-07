package org.topl.traffic.algorithm

import cats.Monad
import fs2.{Chunk, Pipe, Stream}
import org.topl.traffic.protocol.models.{Intersection, Point, WeightedGraph}
import org.topl.traffic.settings.ServiceSettings
import org.topl.traffic.streaming.CompileStream
import tofu.syntax.monadic._
import tofu.syntax.streams.compile._

// Transforms: Graph[InterSection] => shortest-path map for all vertices in graph
trait PathT[F[_], V, W] {
  def findShortestPath(graph: WeightedGraph[V], settings: ServiceSettings): F[Map[W, List[V]]]
}

object PathT {

  type SourceNode = Intersection

  def apply[F[_]: Monad: CompileStream]: PathT[F, Intersection, Point] = new PathT[F, Intersection, Point] {

    def extractSPaths(node: Intersection, parents: Map[Intersection, Intersection]): List[Intersection] =
      parents.get(node).map(p => node +: extractSPaths(p, parents)).getOrElse(List(node))

    override def findShortestPath(
      graph: WeightedGraph[Intersection],
      settings: ServiceSettings
    ): F[Map[Point, List[Intersection]]] =
      for {
        sSteps <- collectShortStep(graph, settings.pathChunkSize)
        sPaths <- buildShortestPaths(sSteps, graph.vertices, settings.pathChunkSize / 2) // g.e?
      } yield sPaths

    def collectShortStep(
      graph: WeightedGraph[Intersection],
      pathChunkSize: Int
    ): F[List[(SourceNode, ShortStep[Intersection])]] =
      Stream
        .emits[F, Intersection](graph.vertices)
        .chunkN(pathChunkSize)
        .through(mkShortStep(graph))
        .to[List]

    def mkShortStep(
      graph: WeightedGraph[Intersection]
    ): Pipe[F, Chunk[SourceNode], (SourceNode, ShortStep[Intersection])] =
      for {
        chunk <- _
        chunkD = chunk.map(x => (x, Dijkstra(graph, x))).toList
        o <- Stream.emits(chunkD.map { case (s, d) =>
               val ss = d.shortestPath(
                 ShortStep(unProcessed = graph.vertices.toSet, distances = d.sDistances)
               )
               (s, ss)
             })
      } yield o

    def buildShortestPaths(
      sSteps: List[(SourceNode, ShortStep[Intersection])],
      nodes: List[Intersection],
      pathChunkSize: Int
    ): F[Map[Point, List[Intersection]]] =
      Stream
        .emits(sSteps)
        .chunkN(pathChunkSize)
        .through(mkShortPath(nodes))
        .to[List]
        .map(_.toMap)

    def mkShortPath(
      nodes: List[Intersection]
    ): Pipe[F, Chunk[(SourceNode, ShortStep[Intersection])], (Point, List[Intersection])] =
      for {
        chunk <- _
        chunkL = chunk.toList
        o <- Stream.emits(chunkL.flatMap { case (sn, ss) => buildShortestPathFromStartNode(ss, nodes, sn) })
      } yield o

    def buildShortestPathFromStartNode(
      shortStep: ShortStep[Intersection],
      nodes: List[Intersection],
      sourceNode: SourceNode
    ): List[(Point, List[Intersection])] =
      nodes.map { node =>
        val bestestPath = extractSPaths(node, shortStep.parents).reverse
        (Point(sourceNode, node), bestestPath)
      }

  }
}

// Stream[F, Vertex] => new Dijkstra[V](graph, vertex) { => ShortStep[V], sourceNode }
// Stream[F, (ShortStep[V], sourceNode)] => Map[Point, List[Intersection]]
