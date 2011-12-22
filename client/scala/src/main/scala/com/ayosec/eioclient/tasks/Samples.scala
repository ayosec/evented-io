package com.ayosec.eioclient.tasks

import util.Random
import SamplesGenerator._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import com.ayosec.misc.ParallelGenerator.parallel

class Counters {
  var webs = 20
  var users = 10
  var visits = 10000
  var ips = 200
  var paths = 20
  var checks = 1000

  override def toString = ("Counters(" +
    "webs = " + webs +
    " users = " + users +
    " visits = " + visits +
    " ips = " + ips +
    " paths = " + paths +
    " checks = " + checks +
    ")")

}

class Samples(val counters: Counters) {

  class WebSite(val name: String, val user: Auth)

  class Visit(
    val name: String,
    val path: String,
    val ip: String,
    val browser: String,
    val os: String,
    val visitDate: DateTime
  ) {
    import java.net.URLEncoder.{encode => urlencode}

    def asQuery = {
      val dateFormat = DateTimeFormat.forPattern("y-M-d H:m:s");
      (
        "name=" + urlencode(name, "UTF-8") + "&" +
        "path=" + urlencode(path, "UTF-8") + "&" +
        "ip=" + urlencode(ip, "UTF-8") + "&" +
        "browser=" + urlencode(browser, "UTF-8") + "&" +
        "os=" + urlencode(os, "UTF-8") + "&" +
        "visited_at=" + urlencode(dateFormat.print(visitDate), "UTF-8")
      )
    }

    def fmtPart(value: Any, min: Int = 2) = {
      var str = value.toString
      while(str.size < min)
        str = "0" + str
      str
    }

    lazy val hour = fmtPart(visitDate.getHourOfDay) + ":" + fmtPart(visitDate.getMinuteOfHour)
    lazy val day = fmtPart(visitDate.getYear, 4) + fmtPart(visitDate.getMonthOfYear) + fmtPart(visitDate.getDayOfMonth)

    /* Originally this method was implemented with
     *  def get(fieldName: String) = getClass.getMethod(fieldName).invoke(this).asInstanceOf[String]
     * but that was really slow.
     */
    def get(fieldName: String) = fieldName match {
      case "name" => name
      case "path" => path
      case "ip" => ip
      case "browser" => browser
      case "os" => os
      case "hour" => hour
      case "day" => day
      case _ => throw new NoSuchElementException("field not found: " + fieldName)
    }
  }


  val browsers = Vector("Chrome", "Firefox", "Konqueror", "Lynx")
  val oss = Vector("FreeBSD", "Linux", "OpenSolaris")

  val ips = counters.ips.samples(() => 4 samples { () => Random.nextInt(255).toString } mkString "." )
  val paths = counters.paths.samples { () => "/" + randomString(1 + Random.nextInt(6)) }

  val users = counters.users.samples {
    () => new Auth(randomString(Random.nextInt(5) + 5), randomString(Random.nextInt(5) + 5))
  }

  val websites = counters.webs.samples { () => new WebSite(randomString(Random.nextInt(10) + 4), users.sample) }

  val visits = parallel(counters.visits) { (count) =>
    var builder = new Array[Visit](count)

    val referenceTime = 1320000000
    for(i <- 0 until count) {
      val visitDate = new DateTime((referenceTime + util.Random.nextInt(3600 * 24 * 365 * 2)) * 1000L)

      builder(i) = new Visit(
        websites.sample.name,
        paths.sample,
        ips.sample,
        browsers.sample,
        oss.sample,
        visitDate
      )
    }

    builder.toList
  }

}
