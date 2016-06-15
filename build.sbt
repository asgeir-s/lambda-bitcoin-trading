// Project name (artifact name in Maven)
name := "ts"

// orgnization name (e.g., the package name of the project)
organization := "com.cluda"

version := "0.0.1"

// project description
description := "AWS Lambda trading systems"

// Do not append Scala versions to the generated artifacts
crossPaths := false

// This forbids including Scala related libraries into the dependency
autoScalaLibrary := false

// library dependencies. (orginization name) % (project name) % (version)
libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-core" % "1.1.0",
  "com.amazonaws" % "aws-lambda-java-events" % "1.3.0",
  "com.alibaba" % "fastjson" % "1.2.12",
  "com.tictactec" % "ta-lib" % "0.4.0",
  "org.postgresql" % "postgresql" % "9.4.1208.jre7",
  "junit" % "junit" % "4.12"
)

retrieveManaged := true

enablePlugins(AwsLambdaPlugin)

lambdaHandlers := Seq(
  "ts-macd-1" -> "systems.Macd1::handler",
  "ts-aroon-1" -> "systems.Aroon::handler",
  "ts-williamr-1" -> "systems.WilliamR::handler"

)

s3Bucket := Some("ts-lambdas")

region := Some("eu-central-1")

awsLambdaMemory := Some(192)

awsLambdaTimeout := Some(30)

roleArn := Some("arn:aws:iam::525932482084:role/ts-lambda")