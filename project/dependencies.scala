import sbt._

object dependencies {

  import versions._

  val Http4s: List[ModuleID] =
    List(
      "org.http4s" %% "http4s-dsl",
      "org.http4s" %% "http4s-blaze-server",
      "org.http4s" %% "http4s-blaze-client",
      "org.http4s" %% "http4s-circe"
    ).map(_ % Http4sVersion)

  val Circe: List[ModuleID] =
    List(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser",
      "io.circe" %% "circe-refined"
    ).map(_       % CirceVersion) ++ List(
      "io.circe" %% "circe-magnolia-derivation" % CirceMagniliaDerivationVersion
    )

  val Cats: List[ModuleID] = List(
    "org.typelevel" %% "cats-core"   % CatsVersion,
    "org.typelevel" %% "cats-effect" % CatsEffectVersion
  )

  val Fs2: List[ModuleID] = List(
    "co.fs2" %% "fs2-core" % Fs2Version
  )

  val Testing: List[ModuleID] = List(
    "org.scalatest"              %% "scalatest"                 % ScalaTestVersion           % Test,
    "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % ScalaCheckShapelessVersion % Test,
    "org.scalatestplus"          %% "scalacheck-1-15"           % ScalaTestPlusVersion       % Test
  )

  val Tofu: List[ModuleID] = List(
    "tf.tofu"     %% "tofu-core"        % TofuVersion,
    "tf.tofu"     %% "tofu-derivation"  % TofuVersion,
    "tf.tofu"     %% "tofu-fs2-interop" % TofuVersion,
    "org.manatki" %% "derevo-circe"     % DerevoVersion
  )

  val Config: List[ModuleID] = List(
    "com.github.pureconfig" %% "pureconfig"             % PureConfigVersion,
    "com.github.pureconfig" %% "pureconfig-cats-effect" % PureConfigVersion
  )

  val CompilerPlugins: List[ModuleID] =
    List(
      compilerPlugin(
        "org.typelevel" %% "kind-projector" % KindProjector cross CrossVersion.full
      ),
      compilerPlugin(
        "org.scalamacros" % "paradise" % MacroParadise cross CrossVersion.full // remove!
      ),
      compilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
    )

  lazy val core: List[ModuleID] =
    Circe ++ Cats ++ Fs2 ++ Testing ++ Tofu ++ Http4s ++ Config

}
