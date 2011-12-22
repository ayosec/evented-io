
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import com.ayosec.bindize._

class BindizeParserSpec extends FlatSpec with ShouldMatchers {

  it should "parse options with no arguments" in {
    var optOne = false
    var optO = false
    var optTwo = false

    val parser = new Parser() {
      the option "--one" is { optOne = true }
      the option "-o"    is { optO = true }
      the option "--two" is { optTwo = true }
    }

    // The constructor should not invoke any method
    optOne should be (false)
    optO   should be (false)
    optTwo should be (false)

    // Only -o is present
    parser.parse(List("-o"))
    optOne should be (false)
    optO   should be (true)
    optTwo should be (false)

    // All options present
    optO = false

    parser.parse(List("--one", "-o", "--two"))
    optOne should be (true)
    optO   should be (true)
    optTwo should be (true)

  }

  it should "raise an exception when a unregistered option is present" in {
    val parser = new Parser()
    evaluating { parser.parse(List("--any")) } should produce [UnkownOptionException]
  }

  it should "call a custom unkownOption method if there one defined" in {
    val parser = new Parser() {
      var customMethodUsed = false

      override def unkownOption(opts: Iterable[String]) = {
        this.customMethodUsed = true
        opts.tail
      }

      var optA = false
      var optB = false

      the option "-a" is { this.optA = true }
      the option "-b" is { this.optB = true }
    }

    parser.parse(List("-a", "any", "-b"))
    parser.customMethodUsed should be (true)
    parser.optA should be (true)
    parser.optB should be (true)
  }

  it should "pass arguments when the options wants them" in {

    val parser = new Parser() {
      var verbose = false
      var noCount = 0
      var fileName = ""

      the option "-v"     is { this.verbose = true }
      the option "--no"   is { this.noCount = 1 }

      the option "--file" withParam { this.fileName = _ }
    }

    parser.parse(List("-v", "--file", "foo"))

    parser.verbose should be (true)
    parser.noCount should be (0)
    parser.fileName should be ("foo")

  }

  it should "raise an exception if an option does not receive its params" in {
    val parser = new Parser() {
      the option "--any" withParam { (x: String) => x  }
    }

    evaluating { parser.parse(List("--any")) } should produce [NotEnoughArgumentsForOptionException]
  }

  it should "define an option with multiple names using a List" in {
    val parser = new Parser() {
      var verbose = false

      the option List("-v", "--verbose") is { this.verbose = true }
    }

    parser.parse(List("-v"))
    parser.verbose should be (true)

    parser.verbose = false
    parser.parse(List("--verbose"))
    parser.verbose should be (true)

    evaluating { parser.parse(List("--v")) } should produce [UnkownOptionException]
  }

  it should "define an option with multiple names using options/and/are" in {
    val parser = new Parser() {
      var verbose = false

      the options "-v" and "--verbose" are { this.verbose = true }
    }

    parser.parse(List("-v"))
    parser.verbose should be (true)

    parser.verbose = false
    parser.parse(List("--verbose"))
    parser.verbose should be (true)

    evaluating { parser.parse(List("--v")) } should produce [UnkownOptionException]
  }

}
