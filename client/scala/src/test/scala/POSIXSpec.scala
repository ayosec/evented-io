
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import java.lang.IllegalArgumentException
import sys.process.Process

import com.ayosec.eioclient.serverimpls.POSIX

class POSIXSpec extends FlatSpec with ShouldMatchers {

  import sys.process._

  it should "remove a directory with all its content" in {
    "mkdir -p /tmp/thisisatestjustremoveme/a/b/c" #&& "touch /tmp/thisisatestjustremoveme/a/b/c/d" !

    val dir = new java.io.File("/tmp/thisisatestjustremoveme")
    dir.exists should be (true)
    POSIX.delete(dir)
    dir.exists should be (false)
  }

  // Specs for process/port management

  it should "raises an exception when isPortAvailable receives an invalid port" in {
    evaluating { POSIX.isPortAvailable(70000) } should produce [IllegalArgumentException]
  }


  final val freePort = 60123

  def runTime(cb: () => Unit) = {
    val start = System.currentTimeMillis
    cb()
    (System.currentTimeMillis - start).toInt
  }

  it should "returns inmediatly when the port is available" in {
    runTime { () => POSIX.killAtPortAndWait(freePort) } should be < (100)
  }

  it should "wait to release the port when it was in use" in {
    // Creates a new process listening on our port
    POSIX.isPortAvailable(freePort) should be (true)
    Process("nc -l -p " + freePort).run
    Thread.sleep(50)
    POSIX.isPortAvailable(freePort) should be (false)

    runTime { () => POSIX.killAtPortAndWait(freePort) } should be < (100)
  }

  it should "wait to have a port available" in {
    POSIX.isPortAvailable(freePort) should be (true)

    runTime { () =>
      Process(List("sh", "-c", "sleep 0.3; nc -l -p " + freePort)).run
      POSIX.waitForPortAvailable(freePort)
    } should be > (500)

  }

}
