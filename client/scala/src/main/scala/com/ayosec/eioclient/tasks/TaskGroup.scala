package com.ayosec.eioclient.tasks

trait TasksGroup {
  final val GET = "GET"
  final val POST = "POST"

  val url: String
  val name: String

  val tasks: List[Task]
}
