package com.ayosec.eioclient.serverimpls

import java.io.File
import sys.process.Process

import com.ayosec.procfs.TCPInfo
import com.ayosec.eioclient.tasks.Task

class ServerImplementation(val path: String) {

  val implPort = 12000
  val mongodbPort = 27017

  def launch(taskGroups: List[List[Task]]) = {

    // Force a stop of any previous server and create a new one
    val proc = TCPInfo.processOnPort(implPort)
    if(proc != null) {
      proc.sendSignal(15)
      TCPInfo.waitForFreePort(implPort)
    }

    // Create a new MongoDB instance and starts it. Wait a second
    // for the MongoDB initialization
    val mongodbServer = new MongoDBServer(mongodbPort) {{ startAndWait }}

    localServer("start")
    TCPInfo.waitForReadyPort(implPort)

    val resourceWatcher = ResourceWatcher.watch(implPort)

    // Launch requests
    val machineResult = taskGroups flatMap { (tasks) =>
      val machine = new RequestMachine(100, implPort, tasks)
      machine.run
    }

    // Stop everything
    localServer("stop")
    mongodbServer.stop

    // Returns an anonymous pseudo-struct
    new {
      val requests = machineResult
      val resources = resourceWatcher()
    }
  }


  def localServer(arg: String) { localServer(List(arg)) }

  def localServer(args: List[String]) {
    Process("./server" :: args, new java.io.File(path), "PORT" -> implPort.toString) !
  }

}

