package com.ayosec.bindize

import collection.mutable.{MutableList => MList}

class ParserException extends Exception
class UnkownOptionException(val optionName: String) extends ParserException
class NotEnoughArgumentsForOptionException(val optionName: String) extends ParserException
class InvalidValueForOptionException(val optionName: String, val value: String) extends ParserException

class Parser {

  protected val options = MList[OptionSpec]()

  def registerOption[T <: OptionSpec](newOption: T) = {
    options += newOption
    newOption
  }

  protected final class TheWord(parser: Parser) {
    def option(name: String) = new SingleNameOption(parser, name)
    def option(name: List[String]) = new MultipleNamesOption(parser, name)

    def options(firstName: String) = new MultipleNamesOption(parser, List(firstName))
  }

  protected val the = new TheWord(this)


  // By default, throws an exception when an option is not registered
  def unkownOption(opts: Iterable[String]): Iterable[String] = throw new UnkownOptionException(opts.head)

  // So,
  def parse(opts: Iterable[String]): Parser =
    if(opts.isEmpty)
      this
    else
      parse(options find { _.matchs(opts.head) } match {
        case Some(spec) => spec.invoke(opts.head, opts.tail)
        case None => unkownOption(opts)
      })
}

