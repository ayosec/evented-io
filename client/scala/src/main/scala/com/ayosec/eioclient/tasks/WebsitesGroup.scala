package com.ayosec.eioclient.tasks

class WebsitesGroup(samples: Samples) extends TasksGroup {
  val name = "Websites"
  val url = "/websites"

  lazy val tasks = samples.websites.par.map { (website) =>
    new Task(
      Request(POST, url, Map("name" -> website.name), website.user),
      Response(200, new HasKey("website_id"))
    )
  }.toList

}

class WebsiteValidationsGroup(samples: Samples) extends TasksGroup {
  val name = "WebsiteValidations"
  val url = "/websites"

  lazy val tasks = List(
    new Task(Request(POST, url), Response(401)),
    new Task(Request(POST, url, Map("name" -> "aaa"), samples.users(0)), Response(403)),
    new Task(Request(POST, url, Map("name" -> samples.websites(0).name), samples.websites(0).user), Response(403))
  )

}
