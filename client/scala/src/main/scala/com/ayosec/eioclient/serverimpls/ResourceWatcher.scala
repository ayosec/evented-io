package com.ayosec.eioclient.serverimpls

import actors.Futures.future
import com.ayosec.procfs.TCPInfo

object ResourceWatcher {

  def watch(port: Int) = future {
    var items = List[ResourceItem]()
    val process = TCPInfo.processOnPort(port)

    if(process != null) {
      process.getCPUUsage // Just to initialize everything

      var continue = true

      while(continue) {

        // Get the CPU usage. If the value is negative the process has gone
        val cpuUsage = process.getCPUUsage

        if(cpuUsage < 0) {
          continue = false
        } else {
          val statMem = process.getStatMemory
          items = items :+ new ResourceItem(
            System.currentTimeMillis,
            cpuUsage,
            statMem.getTotalSize,
            statMem.getResident,
            statMem.getShare
          )

          Thread.sleep(300)
        }
      }
    }

    items
  }

}

class ResourceItem(
  val timestamp: Long,
  val cpuUsage: Double,
  val memSize: Long,
  val memResident: Long,
  val memShared: Long)

