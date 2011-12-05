package com.ayosec.eioclient.tasks

import collection.immutable._;

object SamplesGenerator {

  class SeqWrapper[T](val source : Seq[T]) {
    def sample = source(util.Random.nextInt(source.length))
  }

  implicit def wrapSeq[T](source: Seq[T]) = new SeqWrapper(source)

  class SampleIterator(val times : Int) {
    def samples[B](f: () => B) = {
      var builder = new VectorBuilder[B]
      builder.sizeHint(times)
      for(i <- 1 to times) builder += f()
      builder.result
    }
  }

  implicit def createIterator(times: Int) = new SampleIterator(times)


  def randomString(length: Int) = length.samples(() => chars.sample).mkString

  val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toList

}
