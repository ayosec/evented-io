package com.ayosec.eioclient.tasks

class WebsitesGroup(samples: Samples) extends TasksGroup {
  val name = "Websites"
  val url = "/websites"

  lazy val tasks = {
    val validations = List(
      new Task(Request(POST, url), Response(401)),
      new Task(Request(POST, url, Map("name" -> "aaa"), samples.users(0)), Response(403)),
      new Task(Request(POST, url, Map("name" -> samples.users(0).user, "password" -> "-")), Response(403))
    )

    val websites = samples.websites.par.map { (website) =>
      new Task(
        Request(POST, url, Map("name" -> website.name), website.user),
        Response(200, new HasKey("user_id"))
      )
    }


    validations ++ websites
  }
}
