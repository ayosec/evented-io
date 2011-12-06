package com.ayosec.eioclient.tasks

class UsersGroup(val samples: Samples) extends TasksGroup {
  val url = "/users"
  val name = "Users"

  lazy val tasks = {
    val validations = List(
      new Task(Request(POST, url), Response(403)),
      new Task(Request(POST, url, Map("name" -> "aaa", "password" -> "bbbbbbbbbbbb")), Response(403)),
      new Task(Request(POST, url, Map("name" -> samples.users(0).user, "password" -> "bbbbbbbbbbbb")), Response(403))
    )

    val users = samples.users.par.map { (user) =>
      new Task(
        Request(POST, url, Map("name" -> user.user, "password" -> user.password)),
        Response(200, new HasKey("user_id"))
      )
    }

    validations ++ users
  }
}
