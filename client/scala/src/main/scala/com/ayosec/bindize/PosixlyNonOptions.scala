package com.ayosec.bindize

trait PosixlyNonOptions extends Parser {
  private[this] var lastArgumentSeen = Iterable[String]()

  def arguments = lastArgumentSeen

  override def unkownOption(opts: Iterable[String]) = {
    if((opts.head)(0) == '-') {
      super.unkownOption(opts)
    } else {
      this.lastArgumentSeen = opts
      Nil
    }
  }
}
