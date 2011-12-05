package com.ayosec.eioclient.tasks

import SamplesGenerator._
import ParallelGenerator.parallel

class VisitsGroup(val samples: Samples) {
  final val url =  "/visit"
  final val GET = "GET"

  val tasks = {
    val validations = List(
      new Task(Request(GET, url, Map("name" -> "x")), Response(404))
    )

    val visits = parallel(samples.counters.visits) { (count) =>
      var visits = new Array[Task](count)

      val referenceTime = 1320000000
      val dateFormat = new java.text.SimpleDateFormat("y-M-d H:m:s")
      for(i <- 0 until count) {
        val visitDate = new java.util.Date((referenceTime + util.Random.nextInt(3600 * 24 * 365 * 2)) * 1000L)
        val data = Map(
          "name" ->  samples.websites.sample.name,
          "path" ->  samples.paths.sample,
          "ip" ->  samples.ips.sample,
          "browser" ->  samples.browsers.sample,
          "os" ->  samples.oss.sample,
          "visited_at" ->  dateFormat.format(visitDate)
        )

        visits(i) = new Task(Request(GET, url, data), Response(200))
      }

      visits.toList
    }

    validations ++ visits
  }
}
