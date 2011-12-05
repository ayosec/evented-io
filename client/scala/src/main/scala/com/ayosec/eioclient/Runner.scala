package com.ayosec.eioclient

object Runner extends App {

  def buildGeneratorFromArgs = {
    val counters = new tasks.Counters

    // Parse command line
    val counterMatch = "^(webs|users|visits|ips|paths|checks)=(\\d+)$".r
    for(arg <- args) {
      arg match {
        case counterMatch(name, valueStr) => {
          val value = valueStr.toInt
          name match {
            case "webs" => counters.webs = value
            case "users" => counters.users = value
            case "visits" => counters.visits = value
            case "ips" => counters.ips = value
            case "paths" => counters.paths = value
            case "checks" => counters.checks = value
          }
        }
        case _ => {
          println("Unknown argument: " + arg)
          System.exit(1)
        }
      }
    }

    new tasks.TaskGenerator(new tasks.Samples(counters))
  }

  val taskGenerator = buildGeneratorFromArgs
  for (group <- taskGenerator.groups) {
    println("--- GROUP ---")
    for(task <- group)
      println(task)
  }
  println( taskGenerator.groups.foldLeft(0) { _ + _.size } )
}
