package org.topl.traffic

import fs2.Stream
import tofu.streams.Compile

object streaming {

  type CompileStream[F[_]] = Compile[Stream[F, *], F]

}
