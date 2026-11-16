name := """product-service"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.13.18"

libraryDependencies ++= Seq(
  guice,
  javaJdbc,
  
  // SQL Server JDBC Driver
  "com.microsoft.sqlserver" % "mssql-jdbc" % "12.4.2.jre11",
  
  // JSON processing - compatible with Play Framework 3.0.12
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.14.3",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.14.3"
)
