name := "tipa-play-auth"

organization := "net.tiflotecnia.tipa"

version := "0.2"

scalaVersion := "2.11.1"


resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
  Resolver.url(
    "Typesafe Ivy Snapshots",
    url("http://repo.typesafe.com/typesafe/ivy-snapshots/"))(Resolver.ivyStylePatterns)
//tipaRepository
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % "2.3.1"
)

val tipaRepository = Resolver.ssh("TIPA", "tipa.tiflotecnia.net", "/srv/ivy/releases") as("ragb", new File(Path.userHome / ".ssh" / "id_dsa" toString)) withPermissions("0664")

publishTo := Some(tipaRepository)