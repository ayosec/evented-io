package com.ayosec.eioclient.tasks

import SamplesGenerator._

class VisitsGroup(val samples: Samples) extends TasksGroup {
  val url = "/visit"
  val name = "Visits"

  lazy val tasks = {
    val validations = List(
      new Task(Request(GET, url, Map("name" -> "x")), Response(404))
    )

    val visits = samples.visits.par.map { (visit) =>
      new Task(Request(GET, url, visit.asQuery), Response(200))
    }


    validations ++ visits
  }
}
