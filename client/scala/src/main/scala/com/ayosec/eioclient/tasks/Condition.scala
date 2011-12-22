package com.ayosec.eioclient.tasks
import net.liftweb.json.JsonAST.JNothing
import net.liftweb.json.{JValue, parse}

abstract class Condition {
  def validates(response: JValue) : Boolean

  def validates(response: String) : Boolean = validates(parse(response))
}

class HasKey(keyName: String) extends Condition {
  def validates(response: JValue) = (response \ keyName) != JNothing
  override def toString = "HasKey(" + keyName + ")"
}

class CompareSet(set: SetComparator) extends Condition {
  def validates(response: JValue) = set.sameContent(response)
  override def toString = "CompareSet(...)"
}

object NoCondition extends Condition {
  def validates(response: JValue) = true
  override def validates(response: String) = true
  override def toString = "NoCondition"
}

trait SetComparator {
  def sameContent(that: JValue): Boolean
}
