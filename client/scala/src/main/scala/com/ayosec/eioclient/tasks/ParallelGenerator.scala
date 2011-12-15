package com.ayosec.eioclient.tasks

//import scala.actors.Future
import actors.Futures._

object ParallelGenerator {
  // Computes how many items has to do every thread
  def segmentsSize(count: Int) = {
    val procs = Runtime.getRuntime.availableProcessors
    val countPerProc = count / procs

    // All segments will have the same size, except the last one, which will
    // have the pending items.
    // For example, with count = 10 and availableProcessors = 4 the generated
    // list is (2, 2, 2, 4)
    List.fill(procs - 1) { countPerProc } ++ List(count - (procs - 1) * countPerProc)
  }

  def parallel[T](count: Int)(callback: (Int) => Iterable[T]) = {
    // Generate one thread for every segment
    val futures = segmentsSize(count) map { (count) => future { callback(count) } }

    // Wait for the result of every thread and concatenate them
    futures flatMap { _ apply }
  }
}
