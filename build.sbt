name := "tipa-play-auth"

organization := "net.tiflotecnia.tipa"

version := "0.4.1"

scalaVersion := "2.11.8"


resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Typesafe Snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
  Resolver.url(
    "Typesafe Ivy Snapshots",
    url("http://repo.typesafe.com/typesafe/ivy-snapshots/"))(Resolver.ivyStylePatterns)
//tipaRepository
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play" % "2.5.3"
)

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
