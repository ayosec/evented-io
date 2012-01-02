package com.ayosec.easyxml

import javax.xml.stream._
import javax.xml.stream.events.{StartElement, EndElement, XMLEvent}
import javax.xml.namespace.QName

object Builder {

  def apply(pathName: String, root: String) = new Builder(new java.io.FileOutputStream(pathName), root)

  def apply(file: java.io.File, root: String) = new Builder(new java.io.FileOutputStream(file), root)

  def apply(stream: java.io.OutputStream, root: String) = new Builder(stream, root)

}

package subcontent {

  class TagWithContent(val tag: Tag, val content: (Builder) => Unit)

  class Tag(val open: StartElement, val close: EndElement) {
    def ~(content: (Builder) => Unit) = new TagWithContent(this, content)
  }

}

class Builder(
  val output: java.io.OutputStream,
  val rootTagName: String) {

  private val factory = XMLEventFactory.newInstance

  private val writer = {
    val writer = XMLOutputFactory.newInstance.createXMLEventWriter(output)

    writer.add(factory.createStartDocument)
    writer.add(factory.createStartElement("", "", rootTagName))
    writer
  }

  def close() {
    writer.add(factory.createEndElement("", "", rootTagName))
    writer.add(factory.createEndDocument)
    output.close()
  }

  /*
   * Helpers to predefine content
   */

  import subcontent._

  def tag(tagName: String) = {
    new Tag(factory.createStartElement("", "", tagName), factory.createEndElement("", "", tagName))
  }

  def cdata(content: String) = factory.createCData(content)

  def attribute(name: String, value: Any) = factory.createAttribute(new QName(name), value.toString)

  def attribute(name: String, value: Option[Any]) =
    if(value.isEmpty)
      None
    else
      Some(factory.createAttribute(new QName(name), value.get.toString))

  def attribute(name: String): (Any) => Option[XMLEvent] = { (value: Any) =>
    if(value.isInstanceOf[Option[_]])
      attribute(name, value.asInstanceOf[Option[_]])
    else
      Some(attribute(name, value))
  }

  /*
   * “Appenders”
   */

  def <<(anyText: String) = {
    writer.add(factory.createCharacters(anyText))
    this
  }

  def <<(tag: Tag) = {
    writer.add(tag.open)
    this
  }

  def <<(event: XMLEvent) = {
    writer.add(event)
    this
  }

  def <<(tagWithContent: TagWithContent) = {
    writer.add(tagWithContent.tag.open)
    tagWithContent.content(this)
    writer.add(tagWithContent.tag.close)
    this
  }

  def <<(option: Option[XMLEvent]) = {
    if(!option.isEmpty)
      writer.add(option.get)
    this
  }

  def <<!(cdata: String) = {
    writer.add(factory.createCData(cdata))
    this
  }

}
