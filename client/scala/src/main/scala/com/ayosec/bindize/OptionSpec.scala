package com.ayosec.bindize

abstract class OptionSpec(parser: Parser) {

  def matchs(optionName: String): Boolean

  protected var code: OptionCode = null

  protected def register(code: OptionCode) {
    this.code = code
    parser.registerOption(this)
  }

  def invoke(name: String, opts: Iterable[String]) = code.invoke(name, opts)

  def is(callback: => Unit) { register(new OptionCodeWithNoArgs(callback _)) }

  def withParam(callback: (String) => Unit) { register(new OptionCodeWithOneString(callback)) }

}

class MultipleNamesOption(parser: Parser, val names: List[String]) extends OptionSpec(parser) {
  def matchs(optionName: String) = names exists { _ == optionName }

  def and(optionName: String) = new MultipleNamesOption(parser, names :+ optionName)
  def are(callback: => Unit) { is(callback) }
}

class SingleNameOption(parser: Parser, val name: String) extends OptionSpec(parser) {
  def matchs(optionName: String) = optionName == name
}
