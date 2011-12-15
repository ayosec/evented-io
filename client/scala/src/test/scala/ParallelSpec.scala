
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import com.ayosec.eioclient.tasks.ParallelGenerator._

class POSIXSpec extends FlatSpec with ShouldMatchers {

  it should "generate as many items as requested" in {

    (parallel(3) { (n) => 1 to n } size) should be (3)
    (parallel(12) { (n) => 1 to n } size) should be (12)
    (parallel(20) { (n) => 1 to n } size) should be (20)
    (parallel(200) { (n) => 1 to n } size) should be (200)

  }

}
