
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import com.ayosec.bindize._

class BindizeExamplesSpec extends FlatSpec with ShouldMatchers {

  it should "parse a full example" in {

    val parser = new Parser with NonOptionArguments {
      var verbose    = false
      var timer      = false
      var anything   = 0
      var debugLevel = 0
      var configFile = ""
      var dirs       = List[String]()

      the option "-v" is { verbose = true }
      the option "-t" is { timer = true }
      the option "-a" is { anything += 1 }

      the option "-c" withParam { configFile = _ }
      the option "-d" withParam { (value) => debugLevel = value.toInt }

      the options "-D" and "--dir" withParam { (value) => dirs = dirs :+ value }
    }

    parser.parse("-a -v -a -d 5 -c file.conf nonoption0 -D a -D b --dir c nonoption1 nonoption2".split(" "))

    // Check the results
    parser.verbose    should be (true)
    parser.timer      should be (false)
    parser.anything   should be (2)
    parser.debugLevel should be (5)
    parser.configFile should be ("file.conf")
    parser.dirs       should be (List("a", "b", "c"))
    parser.arguments  should be (List("nonoption0", "nonoption1", "nonoption2"))

  }
}
