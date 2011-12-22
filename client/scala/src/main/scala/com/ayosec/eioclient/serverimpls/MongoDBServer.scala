package com.ayosec.eioclient.serverimpls

import sys.process._
import java.io.File
import com.ayosec.misc.POSIX

class MongoDBServer(val port: Int) {

  val dbRootPath = new File(
    List(
      "/tmp/test-eio/mongodb",
      System.currentTimeMillis,
      util.Random.nextInt(10000)
    ).mkString("-"))

  def dbPath(path: String) = dbRootPath.getPath + "/" + path

  def stop {
    POSIX.killAtPort(port)

    if(dbRootPath.exists)
      POSIX.delete(dbRootPath)
  }

  def start = {
    new File(dbRootPath, "data").mkdirs

    Process(List("mongod",
      "--fork",
      "--nojournal",
      "--noprealloc",
      "--nohttpinterface",
      "--noauth",
      "--port", port.toString,
      "--dbpath", dbPath("data"),
      "--pidfilepath", dbPath("server.pid"),
      "--logpath", dbPath("server.log"))).run
  }

  def startAndWait {
    start
    POSIX.waitForPortReady(port)
  }

}
