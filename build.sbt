import sbtrelease._

sbtPlugin := true

name := "sbt-mdrw"

version := "1.0.0-SNAPSHOT"

organization := "org.eigengo"

scalaVersion := "2.9.2"

/** Shell */
shellPrompt := { state => System.getProperty("user.name") + "> " }

shellPrompt in ThisBuild := { state => Project.extract(state).currentRef.project + "> " }

libraryDependencies ++= Seq(
  "org.pegdown"          % "pegdown"           % "1.4.1",
  "org.specs2"          %% "specs2"            % "1.12.3"       %       "test"
)

/** Compilation */
javacOptions ++= Seq("-source", "1.6", "-target", "1.6")

javaOptions += "-Xmx2G -XX:MaxPermSize=1024m"

scalacOptions ++= Seq("-deprecation", "-unchecked")

maxErrors := 20 

pollInterval := 1000

logBuffered := false

cancelable := true

credentials += Credentials(Path.userHome / ".sonatype")

testOptions := Seq(Tests.Filter(s =>
  Seq("Spec", "Suite", "Unit", "all").exists(s.endsWith(_)) &&
    !s.endsWith("FeaturesSpec") ||
    s.contains("UserGuide") || 
    s.contains("index") ||
    s.matches("org.specs2.guide.*")))

/** Console */
initialCommands in console := "import org.eigengo.sbtmdrw._"

publishTo <<= version { v: String =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at nexus + "content/repositories/snapshots")
  else                             Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

publishArtifact in Test := false

pomIncludeRepository := { x => false }

pomExtra := (
  <url>http://www.eigengo.com/</url>
  <licenses>
    <license>
      <name>BSD-style</name>
      <url>http://www.opensource.org/licenses/bsd-license.php</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:eigengo/sbt-mdrw.git</url>
    <connection>scm:git:git@github.com:eigengo/sbt-mdrw.git</connection>
  </scm>
  <developers>
    <developer>
      <id>janmachacek</id>
      <name>Jan Machacek</name>
      <url>http://www.eigengo.com</url>
      </developer>
    <developer>
      <id>anirvanchakraborty</id>
      <name>Anirvan Chakraborty</name>
      <url>http://www.eigengo.com</url>
    </developer>
  </developers>
)