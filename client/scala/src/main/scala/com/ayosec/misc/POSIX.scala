package com.ayosec.misc

import sys.process._

object POSIX {

  // Tools to manage the process related to TCP ports
  def killAtPortAndWait(port: Int) {
    while(!isPortAvailable(port)) {
      killAtPort(port)
      Thread.sleep(500)
    }
  }

  def waitForPortAvailable(port: Int) {
    while(!isPortAvailable(port))
      Thread.sleep(500)
  }

  def waitForPortReady(port: Int) {
    while(isPortAvailable(port))
      Thread.sleep(500)
  }

  def isPortAvailable(port: Int) = {
    try {
      val socket = new java.net.Socket("localhost", port)
      socket.close
      false
    } catch {
      case _: java.net.ConnectException => true
    }
  }

  def killAtPort(port: Int) { ("fuser -sk " + port + "/tcp") !  }

  // Remove the directory and its contents recursively
  def delete(path: java.io.File) {
    if(path.isDirectory) {
      for(file <- path.listFiles) delete(file)
      path.delete
    } else {
      path.delete
    }
  }

}
