package com.ayosec.eioclient.tasks

import SamplesGenerator._

import com.ayosec.misc.ParallelGenerator.parallel
import gnu.trove.map.hash.TObjectIntHashMap
import net.liftweb.json.{JValue, MappingException}


class ChecksGroup(val samples: Samples) extends TasksGroup {

  val url = "/stats"
  val name = "Checks"

  val queries = List("os", "path", "ip", "browser", "hour", "day").subsamples.filter { (s) => s.size > 1 && s.size < 5 }

  lazy val tasks = {
    val validations = List(
      new Task(Request(GET, url, "name=x"), Response(401)),
      new Task(Request(GET, url, "name=x", samples.users.sample), Response(404))
    )

    val checks = parallel(samples.counters.checks) { (count) =>
      val builder = new Array[Task](count)

      // Compute the expected result of the query
      for(i <- 0 until count) {
        val query = queries.sample
        val website = samples.websites.sample

        // Results after map-reduce the visits
        val mapReduce = new MapReduceResult
        for(visit <- samples.visits)
          if(visit.name == website.name)
            mapReduce.increment(query map { (key) => (key -> visit.get(key)) })

        // Related task
        builder(i) = new Task(
          Request(GET, url, "query=" + query.mkString(",") + "&name=" + website.name, website.user),
          Response(200, new CompareSet(mapReduce))
        )
      }

      builder.toList
    }

    validations ++ checks
  }

}

class MapReduceResult extends SetComparator {
  type Key = Seq[Tuple2[String, String]]

  val expected = new TObjectIntHashMap[Key]

  def increment(key: Key) =
    if(!expected.increment(key))
      expected.put(key, 1)

  def put(key: Key, count: Int) = expected.put(key, count)

  // Check equality

  import net.liftweb.json._

  class JsonItem(val count: Int, val key: Map[String,String])

  def hasAllItems(items: List[JsonItem]): Boolean =
    if(items.isEmpty)
      true
    else
      hasItem(items.head) && hasAllItems(items.tail)

  def hasItem(item: JsonItem) = {
    item.count == expected.get(item.key.toSeq)
  }

  def sameContent(that: JValue): Boolean = {
    try {
      implicit val formats = DefaultFormats
      val items = that.extract[List[JsonItem]]

      // This assumes that every item if differente
      items.size == expected.size && hasAllItems(items)
    } catch {
      case e: MappingException => return false
    }
  }
}
