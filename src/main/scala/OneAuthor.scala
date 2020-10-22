import org.apache.hadoop.io
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.{Mapper, Reducer}
import org.xml.sax.SAXParseException

import scala.xml.{Elem, XML}


  // - MAPPER CLASS
  class OneAuthorMapper extends Mapper[LongWritable, Text, Text, Text] {
    override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, Text]#Context): Unit = {
      val XMLoption: Option[Elem] = XMLHandler.parseXML(value)    // Checking for valid input: XML section
      var XMLelem: Elem = null                                    // for xml element if it successfully was parsed
      if (XMLoption.isEmpty) return else XMLelem = XMLoption.get  // If None (meaning wasn't parsed) return
      val contributors = XMLHandler.getContributors(XMLelem)      // Editors or Authors
      if (contributors.length == 1){
        val venue = XMLHandler.getVenue(XMLelem)
        context.write(new Text(venue), value)
      }
    }
  } // end of mapper class


  // - REDUCER CLASS
  class OneAuthorReducer extends Reducer[Text,Text,Text,Text] {
    override
    def reduce(key: Text, values: java.lang.Iterable[Text], context: Reducer[Text, Text, Text, Text]#Context): Unit = {
      values.forEach(XML_text => {
          val title = XMLHandler.getTitle(XMLHandler.parseXML(XML_text).get) // XML_text already passed the parsing test from mapper
          context.write(new Text(title), key)                                // Output: top ten authors for this particular venue
      })
    }
  }// end of reducer class

