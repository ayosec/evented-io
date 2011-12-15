package com.ayosec.eioclient

import tasks._
import serverimpls._
import collection.mutable.MutableList

object Runner extends App {

  class Options {
    val counters = new Counters
    val servers = new MutableList[String]

    def this(args: Array[String]) = {
      // Parse command line
      this()

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
            servers += arg
          }
        }
      }

    }
  }

  val options = new Options(args)

  val servers = options.servers map { new ServerImplementation(_) }

  val tasks = {
    val generator = new TaskGenerator(new Samples(options.counters))
    generator.groups flatMap { (group) =>
      print("[" + group.name + "] ")

      val startTime = System.nanoTime
      val tasks = group.tasks
      println(tasks.size + " tasks generated in " + ((System.nanoTime - startTime) / 1000000) + "ms")
      tasks
    }
  }

  // Run the tests for every implementation
  for(server <- servers) {
    // Force a GC collect to remove previous garbage
    System.gc

    var results = server.launch(tasks)
  }

}
