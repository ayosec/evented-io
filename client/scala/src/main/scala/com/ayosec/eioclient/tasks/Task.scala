package com.ayosec.eioclient.tasks

import net.liftweb.json.JsonAST.JNothing
import net.liftweb.json.JValue
import java.net.URLEncoder.{encode => urlencode}


abstract class Condition {
  def validates(response: JValue) : Boolean
}

class HasKey(keyName: String) extends Condition {
  def validates(response: JValue) = (response \ keyName) != JNothing
  override def toString = "HasKey(" + keyName + ")"
}

class CompareSet(set: Any) extends Condition {
  // TODO
  def validates(response: JValue) = false
  override def toString = "CompareSet(...)"
}

object NoCondition extends Condition {
  def validates(response: JValue) = true
  override def toString = "NoCondition"
}


class Auth(val user: String, val password: String) {
  override def toString = "Auth(" + user + ":" + password + ")"
}

class Request(
    val method: String,
    val path: String,
    val query: Option[String],
    val auth: Option[Auth]) {

  override def toString = {
    var repr = "Request(method=" + method + ", path='" + path + "'"
    query foreach { repr += " query='" + _ + "'"}
    auth foreach { repr += " auth=" + _ }
    repr + ")"
  }
}


object Request {
  def encodeMap(params: Map[String, String]): String =
    params.
    map( (k) => urlencode(k._1, "UTF-8") + "=" + urlencode(k._2, "UTF-8")).
    mkString("&")

  def apply(method: String, path: String) = new Request(method, path, None, None)
  def apply(method: String, path: String, query: String) = new Request(method, path, Some(query), None)
  def apply(method: String, path: String, query: Map[String,String]) = new Request(method, path, Some(encodeMap(query)), None)
  def apply(method: String, path: String, auth: Auth) = new Request(method, path, None, Some(auth))
  def apply(method: String, path: String, query: String, auth: Auth) = new Request(method, path, Some(query), Some(auth))
  def apply(method: String, path: String, query: Map[String,String], auth: Auth) = new Request(method, path, Some(encodeMap(query)), Some(auth))
}

class Response(val status: Int, val conditionOnBody: Condition) {
  override def toString = {
    "Response(status=" + status + ", conditionOnBody=" + conditionOnBody + ")"
  }
}

object Response {
  def apply(status: Int, conditionOnBody: Condition = NoCondition) = new Response(status, conditionOnBody)
}

class Task(val request: Request, val response: Response) {
  override def toString = {
    "Task(" + request + ", " + response + ")"
  }
}
