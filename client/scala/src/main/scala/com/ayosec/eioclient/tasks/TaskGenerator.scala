package com.ayosec.eioclient.tasks

import SamplesGenerator._
//import akka.actor.Actor.spawn
//import akka.dispatch._

class TaskGenerator(samples: Samples) {

  val groups = List(
    new UsersGroup(samples).tasks,
    new WebsitesGroup(samples).tasks,
    new VisitsGroup(samples).tasks
  )

}

