package com.ayosec.eioclient.tasks

import util.Random
import SamplesGenerator._

class Counters {
  var webs = 20
  var users = 10
  var visits = 20000
  var ips = 200
  var paths = 20
  var checks = 2000

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

  val browsers = Vector("Chrome", "Firefox", "Konqueror", "Lynx")
  val oss = Vector("FreeBSD", "Linux", "OpenSolaris")

  val ips = counters.ips.samples(() => 4 samples { () => Random.nextInt(255).toString } mkString "." )
  val paths = counters.paths.samples { () => "/" + randomString(1 + Random.nextInt(6)) }

  val users = counters.users.samples {
    () => new Auth(randomString(Random.nextInt(5) + 5), randomString(Random.nextInt(5) + 5))
  }

  val websites = counters.webs.samples { () => new WebSite(randomString(Random.nextInt(10) + 4), users.sample) }

}
