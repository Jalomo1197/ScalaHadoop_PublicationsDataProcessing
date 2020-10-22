import org.apache.hadoop.io
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.{Mapper, Reducer}
import org.xml.sax.SAXParseException

import scala.xml.{Elem, XML}


// - MAPPER CLASS
class HighestAuthorsPublicationsMapper extends Mapper[LongWritable, Text, Text, Text] {
  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, Text]#Context): Unit = {
    val XMLoption: Option[Elem] = XMLHandler.parseXML(value)   // Checking for valid input: XML section
    var XMLelem: Elem = null                                    // For xml element if it successfully was parsed
    if (XMLoption.isEmpty) return else XMLelem = XMLoption.get  // If None (meaning wasn't parsed) return
    val venue = XMLHandler.getVenue(XMLelem)
    if (!venue.equals("NOTFOUND"))
      context.write(new Text(venue), value)
  }
} // end of mapper class


// - REDUCER CLASS
class HighestAuthorsPublicationsReducer extends Reducer[Text,Text,Text,Text] {
  override
  def reduce(key: Text, values: java.lang.Iterable[Text], context: Reducer[Text, Text, Text, Text]#Context): Unit = {
    var maxAuthors: Int = 0
    var publicationsInfo: List[PublicationInfo] = List()
    var maxContributorPublications: String = ""
    values.forEach(XMLraw => {
      val XMLelem = XMLHandler.parseXML(XMLraw).get
      val contributors = XMLHandler.getContributors(XMLelem)
      val title = XMLHandler.getTitle(XMLelem)
      publicationsInfo = PublicationInfo(title, contributors.length) :: publicationsInfo
      if (contributors.length > maxAuthors) maxAuthors = contributors.length
    })
    publicationsInfo.foreach(info => {
      if (info.contributorAmount == maxAuthors) maxContributorPublications += info.title + ", "
    })
    context.write(key, new Text(maxContributorPublications))
  }

  case class PublicationInfo(title: String, contributorAmount: Int)
}// end of reducer class

