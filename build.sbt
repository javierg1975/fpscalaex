import sbt.Keys.{artifactPath, libraryDependencies, mainClass, managedClasspath, name, organization, packageBin, resolvers, version}

net.virtualvoid.sbt.graph.Plugin.graphSettings

organization := "kaptest"

// Change to actual project name
name := "projectName"

// Adding build time as the version number, to generate the Docker image name with it.
// Ref: http://stackoverflow.com/questions/24191469/how-to-add-commit-hash-to-play-templates
val dateFormat = "yyyy.MM.dd'T'HH.mm.ssZ"
val buildTime = (new java.text.SimpleDateFormat(dateFormat)).format(new java.util.Date())
version := buildTime

scalaVersion := "2.11.7"

wartremoverErrors ++= Seq(
  Wart.Any2StringAdd,
  Wart.EitherProjectionPartial,
  Wart.OptionPartial,
  Wart.Product,
  Wart.Serializable,
  Wart.ListOps
)

wartremoverWarnings ++= Seq(Wart.Any, Wart.Nothing)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8", // yes, this is 2 args
  "-feature",
  "-unchecked",
  "-language:postfixOps",
  //"-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code", // N.B. doesn't work well with the ??? hole
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture"
)

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

// To force scalaVersion, add the following:
ivyScala := ivyScala.value map {
  _.copy(overrideScalaVersion = true)
}

// To help find information about the libraries dependencies (to fix Slug Size issues),
// the "sbt-dependency-graph" plugin was added: https://github.com/jrudolph/sbt-dependency-graph
// Use commands:
// dependency-graph: Shows an ASCII graph of the project's dependencies on the sbt console
// dependency-tree: Shows an ASCII tree representation of the project's dependencies
libraryDependencies ++= {
  val akkaVersion = "2.3.9" //scala v2.11.7
  val sprayVersion = "1.3.3" //scala v2.11.7
  Seq(
    "io.spray" %% "spray-can" % sprayVersion,
    "io.spray" %% "spray-client" % sprayVersion,
    "io.spray" %% "spray-routing" % sprayVersion,
    "io.spray" %% "spray-json" % "1.3.2" force(), //scala v2.11.7
    "net.virtual-void" %% "json-lenses" % "0.6.0",
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "com.logentries" % "logentries-appender" % "1.1.32",
    "hirondelle.date4j" % "date4j" % "1.0" from "http://www.date4j.net/date4j.jar"
  )
}

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
lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

compileScalastyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value

(compile in Compile) <<= (compile in Compile) dependsOn compileScalastyle

// Make 'run' task to depend on 'test' task.
run in Compile <<= (run in Compile).dependsOn(test in Test)

// Provides helpful information about the project.
// Ref: https://github.com/sbt/sbt-buildinfo
// Bad thing is that you cannot compile the project within IntelliJ.
// Ref: https://github.com/sbt/sbt-buildinfo/issues/69
lazy val root: Project = (project in file(".")).
  enablePlugins(BuildInfoPlugin).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoOptions += BuildInfoOption.ToJson,
    buildInfoOptions += BuildInfoOption.BuildTime,
    buildInfoPackage := "whoami",
    buildInfoObject := "buildinfo"
  )

// -- Docker Support:
enablePlugins(DockerPlugin)

// Make docker depend on the package task, which generates a jar file of the application code
docker <<= docker.dependsOn(Keys.`package`.in(Compile, packageBin))

// Make docker depend on 'test' task.
docker <<= docker.dependsOn(test in Test)

dockerfile in docker := {
  val jarFile = artifactPath.in(Compile, packageBin).value
  val classpath = (managedClasspath in Compile).value
  val mainclass = mainClass.in(Compile, packageBin).value.getOrElse(sys.error("Expected exactly one main class"))
  val libs = "/app/libs"
  val jarTarget = "/app/" + jarFile.name
  // The classpath is the 'libs' dir and the produced jar file
  val classpathString = s"$libs/*:$jarTarget"
  // Change to actual port.
  // Will be running on port:
  val port = 8080 //Use the same port set in Boot.scala

  // Change or add remaining env vars as your project requires it.
  // FYI: The environment variables are needed if you plan to "run" the image locally or
  // in an AWS Docker "single container" environment.
  // If you create a "multi container", you still need to add the environment variables
  // in the "Dockerrun.aws.json" config file.
  // QA values:
  val environmentVariables: Map[String, String] = Map(
    "KAPENGINE_BASEPATH" -> "/ke/3.0/",
    "KAPENGINE_CATALOG" -> "/catalog/1.0/",
    "KAPENGINE_HOST" -> "api.qa01.kaptest.net",
    "KAPENGINE_PORT" -> "443",
    "KE_TOKEN" -> "ktpapps:4Rt5S8K3"
  )

  new Dockerfile {
    // Base image
    from("java:8")
    // Expose port
    expose(port)
    // Copy all dependencies to 'libs' in the staging directory
    classpath.files.foreach { depFile =>
      val target = file(libs) / depFile.name
      stageFile(depFile, target)
    }
    // Add the libs dir from the
    addRaw(libs, libs)
    // Add the generated jar file
    add(jarFile, jarTarget)
    // Set the entry point to start the application using the main class
    cmd("java", "-cp", classpathString, mainclass)
    // Add environment variables to image
    env(environmentVariables) //For local test only. For the deployed version on AWS, the env vars are set via 'Dockerrun.aws.json'
  }
}

buildOptions in docker := BuildOptions(
  cache = false,
  removeIntermediateContainers = BuildOptions.Remove.Always
)

imageNames in docker := Seq(
  ImageName(s"${organization.value}/${name.value}:latest"),
  ImageName(s"${organization.value}/${name.value}:${version.value}")
)