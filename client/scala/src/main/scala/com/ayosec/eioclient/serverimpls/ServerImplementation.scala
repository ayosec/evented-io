package com.ayosec.eioclient.serverimpls

import com.ayosec.eioclient.tasks.Task
import sys.process.Process
import com.ayosec.misc.POSIX

class ServerImplementation(val path: String) {

  val implPort = 12000
  val mongodbPort = 27017

  def launch(taskGroups: List[List[Task]]) = {

    // Force a stop of any previous server and create a new one
    POSIX.killAtPortAndWait(implPort)

    // Create a new MongoDB instance and starts it. Wait a second
    // for the MongoDB initialization
    val mongodbServer = new MongoDBServer(mongodbPort) {{ startAndWait }}

    localServer("start")
    POSIX.waitForPortReady(implPort)

    // Launch requests
    val results = taskGroups flatMap { (tasks) =>
      val machine = new RequestMachine(100, implPort, tasks)
      machine.run
    }

    // Stop everything
    localServer("stop")
    mongodbServer.stop

    results
  }


  def localServer(arg: String) { localServer(List(arg)) }

  def localServer(args: List[String]) {
    Process("./server" :: args, new java.io.File(path), "PORT" -> implPort.toString) !
  }

}

