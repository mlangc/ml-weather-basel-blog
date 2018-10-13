name := "wetter-frosch"

version := "0.1"

scalaVersion := "2.12.7"

val dl4jVer = "0.9.1"
libraryDependencies += "org.deeplearning4j" % "deeplearning4j-core" % dl4jVer
libraryDependencies += "org.nd4j" % "nd4j-native-platform" % dl4jVer
libraryDependencies += "org.datavec" % "datavec-api" % dl4jVer

val smileVer = "1.5.1"
libraryDependencies += "com.github.haifengl" %% "smile-scala" % smileVer
libraryDependencies += "com.github.haifengl" % "smile-netlib" % smileVer

val evilPlotVer = "0.4.1"
resolvers += Resolver.bintrayRepo("cibotech", "public")
libraryDependencies += "com.cibo" %% "evilplot" % evilPlotVer
libraryDependencies += "com.cibo" %% "evilplot-repl" % evilPlotVer
libraryDependencies += "com.twelvemonkeys.imageio" % "imageio-core" % "3.4.1"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test

val ammoniteVer = "1.2.1"
libraryDependencies += "com.lihaoyi" %% "ammonite-ops" % ammoniteVer

libraryDependencies += "com.softwaremill.macwire" %% "macros" % "2.3.1" % Provided
libraryDependencies += "at.ipsquare" % "ipsquare-commons-core" % "3.0.1"
libraryDependencies += "io.suzaku" %% "boopickle" % "1.3.0"
libraryDependencies += "commons-io" % "commons-io" % "2.6"
libraryDependencies += "com.storm-enroute" %% "scalameter" % "0.8.2" % Test






