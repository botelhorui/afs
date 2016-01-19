name := "dsi-afs"

version := "1.0"

scalaVersion := "2.11.7"


libraryDependencies += "work.martins.simon" %% "scala-expect" % "1.7.5"

//Dimensions mainly storage (KB, MB, etc)
libraryDependencies += "com.squants"  %% "squants"  % "0.6.1-SNAPSHOT"
//Needed for the squants
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.1" % "test"