package com.ayosec.eioclient.tasks
import net.liftweb.json.JsonAST.JNothing
import net.liftweb.json.JValue

abstract class Condition {
  def validates(response: JValue) : Boolean
}

class HasKey(keyName: String) extends Condition {
  def validates(response: JValue) = (response \ keyName) != JNothing
  override def toString = "HasKey(" + keyName + ")"
}

class CompareSet(set: Any) extends Condition {
  def validates(response: JValue) = set.equals(response)
  override def toString = "CompareSet(...)"
}

object NoCondition extends Condition {
  def validates(response: JValue) = true
  override def toString = "NoCondition"
}

