package com.ayosec.eioclient.serverimpls

import com.ayosec.easyxml.Builder
import com.ayosec.eioclient.tasks.Task
import com.ning.http.client.Response

class TaskResult(val task: Task,
                 val response: Response,
                 val throwable: Throwable,
                 val startTime: Long,
                 val totalTime: Long)


class Report(val outputFileName: String) {

  private val builder = Builder(outputFileName, "results")

  def close() { builder.close() }

  def addServer(server: ServerImplementation, taskGroups: List[List[Task]]) {
    // Launch requests and count the elapsed time
    val startTime = System.currentTimeMillis
    val results = server.launch(taskGroups)
    val totalTime = System.currentTimeMillis - startTime

    val T = builder.tag(_)
    val A = builder.attribute(_:String)

    // (pre)generate objects used to create the XML
    val aPath       = A("path")
    val aTime       = A("time")
    val aDuration   = A("duration")
    val aValid      = A("valid")
    val aStartTime  = A("start-time")
    val aMethod     = A("method")
    val aQuery      = A("query")
    val aAuth       = A("auth")
    val aStatus     = A("status")

    val tServer     = T("server")
    val tRequest    = T("request")
    val tContent    = T("content")
    val tResponse   = T("response")

    // Generate a <server> with all the requests
    builder << tServer << aPath(server.path) << aTime(totalTime)

    for(result <- results) {
      var request = result.task.request
      val isValid = result.task.response.isValid(result.response.getStatusCode,
                                                 result.response.getResponseBody)

      // <request start time="N" valid="true|false">
      builder << tRequest <<
                    aStartTime(result.startTime) <<
                    aDuration(result.totalTime) <<
                    aValid(if(isValid) "true" else "false")

      // Content of the requests
      builder << (tContent ~ { _ <<
        aMethod(request.method) <<
        aPath(request.path) <<
        aQuery(request.query) <<
        aAuth(request.auth)
      })

      // Response (status and body)
      builder << (tResponse ~ { _ <<
        aStatus(result.response.getStatusCode) <<!
        result.response.getResponseBody
      })

      builder << tResponse.close
    }

    builder << tServer.close
  }


}
