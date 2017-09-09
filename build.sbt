organization in ThisBuild := "com.example"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % Test

lazy val `hello-lagom` = (project in file("."))
    .enablePlugins(JavaAppPackaging)
  //  .enablePlugins(JavaServerAppPackaging)
  //  .settings(mainClass in Compile := Some("com.example.hello"))
  //  .enablePlugins(SystemdPlugin)
  .aggregate(`hello-lagom-api`, `hello-lagom-impl`, `hello-lagom-stream-api`, `hello-lagom-stream-impl`)

lazy val `hello-lagom-api` = (project in file("hello-lagom-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      lagomJavadslServer
    )
  )

lazy val `hello-lagom-impl` = (project in file("hello-lagom-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .settings(
    bashScriptExtraDefines ++= Seq(
      """addJava "-Dconfig.file=${app_home}/../conf/application.conf"""",
      """addJava "-Dhttp.port=8080""""
    ),
    resourceDirectory in Compile := baseDirectory.value / "src/main/resources",
    mappings in Universal += {
      ((resourceDirectory in Compile).value / "application.conf") -> "conf/application.conf"
    }
  )
  .dependsOn(`hello-lagom-api`)

lazy val `hello-lagom-stream-api` = (project in file("hello-lagom-stream-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `hello-lagom-stream-impl` = (project in file("hello-lagom-stream-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslTestKit,
      macwire,
      scalaTest
    )
  )
  .dependsOn(`hello-lagom-stream-api`, `hello-lagom-api`)


// ************** settings ******************* //
//lagomServiceGatewayPort in ThisBuild := 8080
//lagomServiceLocatorPort in ThisBuild := 9001

//lagomCassandraEnabled in ThisBuild := false
//lagomUnmanagedServices in ThisBuild := Map("cas_native" -> "http://192.168.0.2:9042")
//lagomCassandraPort in ThisBuild := 9042
//lagomCassandraCleanOnStart in ThisBuild := true

//lagomKafkaEnabled in ThisBuild := false
//lagomKafkaAddress in ThisBuild := "192.168.0.2:9092"
//lagomKafkaPort in ThisBuild := 9092
//lagomKafkaPropertiesFile in ThisBuild :=
// Some((baseDirectory in ThisBuild).value / "project" / "kafka-server.properties")

//lagomKafkaZookeperPort in ThisBuild := 9999
//lagomServicesPortRange in ThisBuild := PortRange(40000, 45000)


//// ************** packaging option(http://www.scala-sbt.org/sbt-native-packager/archetypes/java_app/customize.html) *** //
//bashScriptExtraDefines += """addJava "-Dconfig.file=${app_home}/../conf/application.conf""""
//// add application parameter
//bashScriptExtraDefines += """addApp "--port=8080"""
