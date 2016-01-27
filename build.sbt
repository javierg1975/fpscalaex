organization := "javierg"

name := "FP in Scala exercises"

scalaVersion := "2.12.0-M3"

/*wartremoverErrors ++= Seq(
  Wart.Any2StringAdd,
  Wart.EitherProjectionPartial,
  Wart.OptionPartial,
  Wart.Product,
  Wart.Serializable,
  Wart.ListOps
)*/

//wartremoverWarnings ++= Seq(Wart.Any, Wart.Nothing)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8", // yes, this is 2 args
  "-feature",
  "-unchecked",
  "-language:postfixOps",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code", // N.B. doesn't work well with the ??? hole
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture"
)

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

resolvers ++= Seq("Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Maven Central" at "http://repo1.maven.org/maven2"
)

retrieveManaged := true

// reduce the maximum number of errors shown by the Scala compiler
maxErrors := 20

// increase the time between polling for file changes when using continuous execution
pollInterval := 1000

incOptions := incOptions.value.withNameHashing(true)

// set the prompt (for this build) to include the project id.
shellPrompt in ThisBuild := { state => Project.extract(state).currentRef.project + "> " }

// set the prompt (for the current project) to include the username
shellPrompt := { state => System.getProperty("user.name") + "> " }

// only show 20 lines of stack traces
traceLevel := 20

logLevel := Level.Info

artifactName := { (sv: ScalaVersion, module: ModuleID, artifact: Artifact) =>
  artifact.name + "-" + module.revision + "." + artifact.extension
}

// Run 'scalastyle' at compile
//lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

//compileScalastyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value

(compile in Compile) <<= (compile in Compile) //dependsOn compileScalastyle

// Make 'run' task to depend on 'test' task.
run in Compile <<= (run in Compile).dependsOn(test in Test)
