package com.ayosec.eioclient

import com.ayosec.bindize._
import tasks.Counters

class CommandLine(args: Iterable[String]) {

  val counters = new Counters

  val servers = {
    val parser = new Parser with NonOptionArguments {

      // Counter values
      the option "--webs"   withParam { (value) => counters.webs = value.toInt }
      the option "--users"  withParam { (value) => counters.users = value.toInt }
      the option "--visits" withParam { (value) => counters.visits = value.toInt }
      the option "--ips"    withParam { (value) => counters.ips = value.toInt }
      the option "--paths"  withParam { (value) => counters.paths = value.toInt }
      the option "--checks" withParam { (value) => counters.checks = value.toInt }

      // Load configuration from a file
      the options "--config" and "-C" withParam { (file) =>
        // Treat each line as an argument
        parse(io.Source.fromFile(file).getLines.toIterable)
      }
    }

    parser.parse(args)
    parser.arguments
  }

}
