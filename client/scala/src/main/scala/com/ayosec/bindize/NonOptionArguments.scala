package com.ayosec.bindize

import collection.mutable.MutableList

trait NonOptionArguments extends Parser {
  private[this] var argsBuilder = MutableList[String]()

  def arguments = argsBuilder.toList

  override def unkownOption(opts: Iterable[String]) = {
    val option = opts.head

    if(option(0) == '-')
      super.unkownOption(opts)
    else
      argsBuilder += option

    opts.tail
  }
}
