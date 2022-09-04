package org.topl.traffic.cli

import cats.Monad
import cats.effect.concurrent.Ref
import org.topl.traffic.protocol.models.State
import tofu.syntax.monadic._

// New Source: src, intersection1 intersection2
// Existing Source: intersection1 intersection2 || src => Defined in State
// Default Source:  intersection1 intersection2 || src => Not Defined in State

trait Command
case class WithNewSource(src: String, i1: String, i2: String) extends Command
case class WithExistingSource(i1: String, i2: String) extends Command
case class WithDefaultSource(i1: String, i2: String) extends Command
object Invalid extends Command

object Command {

  def apply[F[_]: Monad](sRef: Ref[F, State], commandLineArray: Array[String]): F[Command] =
    for {
      state <- sRef.get
    } yield (commandLineArray.length, state.jsonSource) match {
      case (3, _)       => WithNewSource(commandLineArray(0), commandLineArray(1), commandLineArray(2))
      case (2, None)    => WithDefaultSource(commandLineArray(0), commandLineArray(1))
      case (2, Some(_)) => WithExistingSource(commandLineArray(0), commandLineArray(1))
      case _            => Invalid
    }

}
