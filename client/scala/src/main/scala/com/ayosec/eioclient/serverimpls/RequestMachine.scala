package com.ayosec.eioclient.serverimpls

import com.ayosec.eioclient.tasks.Task
import java.util.concurrent._
import com.ning.http.client._

class RequestMachine(val concurrency: Int, val port: Int, tasks: List[Task]) {

  def run = {
    val client = {
      val builder = new AsyncHttpClientConfig.Builder()
      val config = builder.setMaximumConnectionsTotal(concurrency)
        .setMaximumConnectionsPerHost(concurrency)
        .setExecutorService(Executors.newCachedThreadPool())
        .setFollowRedirects(false)
        .build

      new AsyncHttpClient(config)
    }

    val semaphore = new Semaphore(concurrency, true)
    val baseUrl = "http://localhost:" + port

    //val latch = new CountDownLatch(args.length)
    val futures = tasks map { (task) =>
      semaphore.acquire

      val url = baseUrl + task.request.path
      val isGet = task.request.isGet
      val request = if(isGet) client.prepareGet(url) else { client.preparePost(url) }

      // Add parameters
      if(!task.request.query.isEmpty) {
        if(isGet) {
          request.setUrl(url + "?" + task.request.query.get)
        } else {
          request.setBody(task.request.query.get)
          request.addHeader("Content-Type", "application/x-www-form-urlencoded")
        }
      }

      // Auth
      if(!task.request.auth.isEmpty) {
        val auth = task.request.auth.get
        val realm = new Realm.RealmBuilder()
                       .setPrincipal(auth.user)
                       .setPassword(auth.password)
                       .setUsePreemptiveAuth(true)
                       .setScheme(Realm.AuthScheme.BASIC)
                       .build();
        request.setRealm(realm)
      }

      request.execute(new HttpHandler(semaphore, task))
    }

    // Wait for all requests
    semaphore.acquire(concurrency)

    // Collect results
    val results = futures map { _.get }
    client.close

    results
  }

  class HttpHandler(semaphore: Semaphore, task: Task) extends AsyncHandler[TaskResult] {

    private val builder = new Response.ResponseBuilder

    val startTime = System.currentTimeMillis
    var totalTime = 0L

    def accumulate(content: HttpContent) = {
      builder.accumulate(content)
      AsyncHandler.STATE.CONTINUE
    }

    def finalize(t: Throwable = null) = {
      semaphore.release
      val totalTime = System.currentTimeMillis - startTime

      new TaskResult(task, builder.build, t, startTime, totalTime)
    }

    def onBodyPartReceived(bodyPart: HttpResponseBodyPart) = accumulate(bodyPart)
    def onStatusReceived(responseStatus: HttpResponseStatus) = accumulate(responseStatus)
    def onHeadersReceived(headers: HttpResponseHeaders) = accumulate(headers)

    def onCompleted() = finalize()
    def onThrowable(t: Throwable) = finalize(t)

  }

}
