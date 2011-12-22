package easyxml

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import com.ayosec.easyxml.Builder

class EasyXMLDSL extends FlatSpec with ShouldMatchers {

  val xmlHead = """<?xml version="1.0" encoding="UTF-8"?>"""

  it should "writes to an ByteArrayOutputStream" in {
    val stream = new java.io.ByteArrayOutputStream
    val builder = Builder(stream, "root")

    val tag = builder.tag("foo")
    builder << tag << "bar" << tag.close
    builder.close

    stream.toString should be (xmlHead + """<root><foo>bar</foo></root>""")
  }

  it should "writes to a file" in {
    val tempFile = java.io.File.createTempFile("test-easyxml", ".xml")

    // Write some XML content
    val builder = Builder(tempFile, "root")
    val tag = builder.tag("foo")
    builder << tag << "bar" << tag.close
    builder.close

    // Check the result
    val expectedString = xmlHead + """<root><foo>bar</foo></root>"""
    val fileReader = new java.io.FileReader(tempFile)
    val fileContent = new Array[Char](expectedString.size)

    fileReader.read(fileContent, 0, expectedString.size)

    fileReader.read should be (-1) // should be EOF
    fileContent.mkString should be (expectedString)
  }

  it should "adds attributes to the last open tag" in {
    val stream = new java.io.ByteArrayOutputStream
    val builder = Builder(stream, "root")

    val tagFoo = builder.tag("foo")
    val tagBar = builder.tag("bar")
    builder <<
      tagFoo <<
        builder.attribute("one", "1") <<
        builder.attribute("two", "2") <<
        "bar" <<
        tagBar <<
          builder.attribute("three", "3") <<
        tagBar.close <<
      tagFoo.close
    builder.close

    stream.toString should be (xmlHead + """<root><foo one="1" two="2">bar<bar three="3"></bar></foo></root>""")
  }

  it should "use attributes as a partially-applied functions" in {

    val stream = new java.io.ByteArrayOutputStream
    val builder = Builder(stream, "root")

    val tag = builder.tag("foo")
    val attr = builder.attribute("one")
    builder << tag << attr("1") << "bar" << tag.close
    builder.close

    stream.toString should be (xmlHead + """<root><foo one="1">bar</foo></root>""")
  }

  it should "accepts a block in tags" in {

    val stream = new java.io.ByteArrayOutputStream
    val builder = Builder(stream, "root")

    val tagFoo = builder.tag("foo")
    val tagBar = builder.tag("bar")
    builder << (tagFoo ~ { _ << (tagBar ~ { _ << "text" }) })
    builder.close

    stream.toString should be (xmlHead + """<root><foo><bar>text</bar></foo></root>""")
  }

  it should "add CDATA sections using <<!" in {
    val stream = new java.io.ByteArrayOutputStream
    val builder = Builder(stream, "root")
    builder << (builder.tag("foo") ~ { _ <<! "raw text" })
    builder << builder.cdata("a < b")
    builder.close

    stream.toString should be (xmlHead + """<root><foo><![CDATA[raw text]]></foo><![CDATA[a < b]]></root>""")
  }

  it should "not add attributes of type None" in {
    val stream = new java.io.ByteArrayOutputStream
    val builder = Builder(stream, "root")
    builder << builder.tag("foo")
    builder << builder.attribute("one", Some("1"))
    builder << builder.attribute("two", None)
    builder << builder.attribute("three")(Some("3"))
    builder << builder.tag("foo").close
    builder.close

    stream.toString should be (xmlHead + """<root><foo one="1" three="3"></foo></root>""")

  }

}
