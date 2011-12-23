package com.ayosec.eioclient

import tasks._
import serverimpls._
import collection.mutable.MutableList

object Runner extends App {

  val options = new CommandLine(args)

  val servers = options.servers map { new ServerImplementation(_) }

  val taskGroups = {
    val generator = new TaskGenerator(new Samples(options.counters))
    generator.groups map { (group) =>
      print("[" + group.name + "] ")

      val startTime = System.nanoTime
      val tasks = group.tasks
      println(tasks.size + " tasks generated in " + ((System.nanoTime - startTime) / 1000000) + "ms")
      tasks
    }
  }

  // Run the tests for every implementation
  val report = new Report("/tmp/out.xml")

  for(server <- servers) {
    // Force a GC collect to remove previous garbage
    System.gc
    report.addServer(server, taskGroups)
  }

  report.close

}
