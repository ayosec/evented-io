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
    System.gc // clean-up

    val tasks = tasksFor(group)
    /*for(task <- group)
      println(task)*/
  }

  def tasksFor(group: tasks.TasksGroup) = {
    print("[" + group.name + "] ")

    val startTime = System.nanoTime
    val tasks = group.tasks
    println(tasks.size + " tasks generated in " + ((System.nanoTime - startTime) / 1000000) + "ms")
    tasks
  }

  //println( taskGenerator.groups.foldLeft(0) { _ + _.size } )
}
