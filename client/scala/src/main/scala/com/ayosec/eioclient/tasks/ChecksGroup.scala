package com.ayosec.eioclient.tasks

import SamplesGenerator._
import ParallelGenerator.parallel
import gnu.trove.map.hash.TObjectIntHashMap
import java.util.HashMap
import net.liftweb.json.JValue

class ChecksGroup(val samples: Samples) extends TasksGroup {

  val url = "/stat"
  val name = "Checks"

  final val queries = List("os", "path", "ip", "browser", "hour", "day").subsamples.filter { _.size > 1 }

  lazy val tasks: List[Task] = {
    val validations = List(
      new Task(Request(GET, url, "name=x"), Response(404))
    )

    val checks = parallel(samples.counters.checks) { (count) =>
      val builder = new Array[Task](count)

      // Compute the expected result of the query
      for(i <- 0 until count) {
        val query = queries.sample
        val website = samples.websites.sample

        // Results after map-reduce the visits
        val mapReduce = new MapReduceResult() with MRComparator

        for(visit <- samples.visits) {
          if(visit.name == website.name) {
            val mapped = new HashMap[String, String](query.size)
            for(key <- query)
              mapped.put(key, visit.get(key))

            if(!mapReduce.increment(mapped))
              mapReduce.put(mapped, 1)
          }
        }

        // Related task
        builder(i) = new Task(
          Request(GET, url, Map("query" -> query.mkString(","), "name" -> website.name)),
          Response(200, new CompareSet(mapReduce))
        )
      }

      builder.toList
    }

    validations ++ checks
  }

  type MapReduceResult = TObjectIntHashMap[HashMap[String, String]]
  trait MRComparator {
    def equals(that: JValue) = false
  }
}
