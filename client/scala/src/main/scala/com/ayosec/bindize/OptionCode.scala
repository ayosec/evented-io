package com.ayosec.bindize

abstract class OptionCode {
  def invoke(name: String, opts: Iterable[String]): Iterable[String]
}

class OptionCodeWithNoArgs(callback: () => Unit) extends OptionCode {
  def invoke(name: String, opts: Iterable[String]) = {
    callback()
    opts
  }
}

class OptionCodeWithOneString(callback: (String) => Unit) extends OptionCode {
  def invoke(name: String, opts: Iterable[String]) = {
    if(opts.isEmpty)
      throw new NotEnoughArgumentsForOptionException(name)

    callback(opts.head)
    opts.tail
  }
}
