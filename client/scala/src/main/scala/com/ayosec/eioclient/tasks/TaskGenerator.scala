package com.ayosec.eioclient.tasks

import SamplesGenerator._

class TaskGenerator(samples: Samples) {

  val groups = List(
    new UsersGroup(samples),
    new UserValidationssGroup(samples),
    new WebsitesGroup(samples),
    new WebsiteValidationsGroup(samples),
    new VisitsGroup(samples),
    new ChecksGroup(samples)
  )

}

