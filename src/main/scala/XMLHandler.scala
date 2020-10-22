import org.apache.hadoop.io.Text
import org.xml.sax.SAXParseException

import scala.xml.{Elem, Node, NodeSeq, SAXParseException, XML}

object XMLHandler {
  def parseXML(XMLraw: Text): Option[Elem] ={
    try {
      val XMLelem = XML.loadString(XMLraw.toString) // May throw SAXParseException
      println("XML section successfully parsed. XML tag: " + XMLelem.label)
      Some(XMLelem)
    }
    catch{
      case e: SAXParseException => println("[WARN] XML section passed to mapper was invalid. Due to XML not able to be parsed.\n      Most likely due to a '&' reference in XML")
        None
    }
  }

  def getVenue(XMLelem: Elem): String = {
    val tagType = XMLelem.label
    var venue : String = ""
    tagType match{
      case "phdthesis" | "mastersthesis" =>  venue = (XMLelem \ "school").text // school tag
      case "book" => venue = (XMLelem \ "publisher").text // publisher tag
      case "article" => venue = (XMLelem \ "journal").text // journal tag
      case "incollection" | "inproceedings" | "proceedings" => venue = (XMLelem \ "booktitle").text // booktitle tag
      case "www" => venue = "WWW" // WWW is the venue for websites
      case _ => venue = "NOTFOUND"
    }
    venue
  }

  /*
      Function to obtain contributors to a publication. Returning editors or authors based on the XML outer tag
   */
  def getContributors(XMLelem: Elem): NodeSeq = {
    val tagType = XMLelem.label
    var contributors : NodeSeq = null
    tagType match {
      case "phdthesis" | "mastersthesis" | "inproceedings" | "article" =>  contributors = XMLelem \ "author" // author tags
      case "book" | "proceedings" | "www" => contributors = XMLelem \ "editor" // editor tags
      case _ => contributors = XMLelem \ "editor" // will be zero length
    }
    contributors
  }

  def getTitle(XMLelem: Elem): String = (XMLelem \ "title").text

  def getYear(XMLelem: Elem): String = (XMLelem \ "year").text

}
