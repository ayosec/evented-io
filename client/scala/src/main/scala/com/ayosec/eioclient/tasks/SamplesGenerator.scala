package com.ayosec.eioclient.tasks

import collection.immutable._;

object SamplesGenerator {

  class SeqWrapper[T](val source : Seq[T]) {
    def sample = source(util.Random.nextInt(source.length))

    def subsamples: Seq[Seq[T]] = subsamples(source)

    def subsamples[T](items: Seq[T]): Seq[Seq[T]] = {
      if(items.isEmpty)
        Seq(Seq[T]())
      else 
        subsamples(items.tail).map { items.head +: _ } ++ subsamples(items.tail)
    } 
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
