// Turn this project into a Scala.js project by importing these settings
enablePlugins(ScalaJSPlugin)

name := "Scala.js Example with jQueryUI"

scalaVersion := "2.11.8"

version := "1.0.0"

resolvers += sbt.Resolver.bintrayRepo("denigma", "denigma-releases") //add resolver

def generateIndexTask(index: String, suffix: String) = Def.task {
  val source = baseDirectory.value / "index-template.html"
  val target = (crossTarget in Compile).value / index
  val log = streams.value.log
  IO.writeLines(target,
    IO.readLines(source).map {
      line => line.replace("{{opt}}", suffix)
    }
  )

  log.info(s"Generate $index with suffix $suffix")
}

def copyCss = Def.task {
  val source = baseDirectory.value / "style.css"
  val target = (crossTarget in Compile).value / "style.css"
  IO.writeLines(target,
    IO.readLines(source)
  )

}

Seq(
  (fastOptJS in Compile, "index-dev.html", "fastOpt"),
  (fullOptJS in Compile, "index.html", "opt")
).map {
  case (task, indexHtml, postfix) =>
    task <<= task.dependsOn(generateIndexTask(indexHtml, postfix), copyCss)
}

mainClass in Compile :=Some("Main")

libraryDependencies += "org.querki" %%% "jquery-facade" % "1.0"

jsDependencies += "org.webjars" % "jquery" % "2.2.1" / "jquery.js" minified "jquery.min.js"

libraryDependencies += "org.denigma" %%% "threejs-facade" % "0.0.74-0.1.7" //add dependency

skip in packageJSDependencies := false

persistLauncher in Compile := true

persistLauncher in Test := false
