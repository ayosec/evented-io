
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import java.lang.IllegalArgumentException

import com.ayosec.misc.POSIX

class POSIXSpec extends FlatSpec with ShouldMatchers {

  import sys.process._

  it should "remove a directory with all its content" in {
    "mkdir -p /tmp/thisisatestjustremoveme/a/b/c" #&& "touch /tmp/thisisatestjustremoveme/a/b/c/d" !

    val dir = new java.io.File("/tmp/thisisatestjustremoveme")
    dir.exists should be (true)
    POSIX.delete(dir)
    dir.exists should be (false)
  }

}
