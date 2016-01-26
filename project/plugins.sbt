addSbtPlugin("org.brianmckenna" %% "sbt-wartremover" % "0.13")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.7.0")

//Ref: https://github.com/jrudolph/sbt-dependency-graph
addSbtPlugin("net.virtual-void" %% "sbt-dependency-graph" % "0.7.5")

// Provides access to build.sbt information within the code.
// https://github.com/sbt/sbt-buildinfo
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.5.0")

// SBT - Docker Support
// https://github.com/marcuslonnberg/sbt-docker
addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.2.0")