import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.{Mapper, Reducer}
import org.xml.sax.SAXParseException

import scala.annotation.tailrec
import scala.xml.{Elem, XML}

// - MAPPER CLASS
class VenueTopTenMapper extends Mapper[LongWritable, Text, Text, Text] {
  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, Text]#Context): Unit = {
    val XMLoption: Option[Elem] = XMLHandler.parseXML(value)    // Checking for valid input: XML section
    var XMLelem: Elem = null                                    // for xml element if it successfully was parsed
    if (XMLoption.isEmpty) return else XMLelem = XMLoption.get  // If None (meaning wasn't parsed) return
    val venue = XMLHandler.getVenue(XMLelem)                    // Venue determined by outer tag type
    context.write(new Text(venue), value)
  }
} // end of mapper class


// - REDUCER CLASS
class VenueTopTenReducer extends Reducer[Text,Text,Text,Text] {

  override
  def reduce(key: Text, values: java.lang.Iterable[Text], context: Reducer[Text, Text, Text, Text]#Context): Unit = {
    // Map to store authors appearances in articles
    var contributorMap: Map[String, Int] = Map()
    // Iterating through the article pertaining to venue
    values.forEach(XMLraw =>
    {
        val contributors = XMLHandler.getContributors(XMLHandler.parseXML(XMLraw).get)
        for (c <- contributors) {                       // Summing all authors appearances
          val contributor = c.text                      // Contributor's name
          if (contributorMap.contains(contributor))     // Updating map accordingly
            contributorMap += (contributor -> (contributorMap(contributor) + 1))
          else
            contributorMap += (contributor -> 1)
        }
    })
    // Now find top ten authors for this venue
    val topTen: List[Contributor] = List(new Contributor("-", 0), new Contributor("-", 0), new Contributor("-", 0), new Contributor("-", 0), new Contributor("-", 0), new Contributor("-", 0), new Contributor("-", 0),new Contributor("-", 0),new Contributor("-", 0),new Contributor("-", 0),new Contributor("-", 0))
    for( (contributor, appearances) <- contributorMap){
      println("Author: " + contributor + " Count: " + appearances + " Venue: " + key.toString)
      val IndexOfLowest = find_ContributorWithLowestAppearances(topTen)
      if (topTen(IndexOfLowest).getAppearances() < appearances)
        topTen(IndexOfLowest).setAppearances(appearances).setName(contributor)
    }
    // building output string
    var topTenContributors = ""
    for (author <- topTen){
      topTenContributors += author.getName() + ", "
    }
    context.write(key, new Text(topTenContributors)) // Output: top ten authors for this particular venue
  }

  /* class for used for storing and selection of authors */
  class Contributor(var name: String, var appearances: Int){
    def getAppearances(): Int = appearances
    def getName(): String = name
    def setName(_name: String): Contributor = {name = _name
      this
    }
    def setAppearances(_appearances: Int): Contributor = {appearances = _appearances
      this
    }
  }

  /*
   * Returns the index of the author with lowest appearances. Needed for
   * Comparison with other authors. That Author may be replaced
  */
  def find_ContributorWithLowestAppearances(topTen: List[Contributor]): Int ={
    @tailrec
    def _findLowest(_topTen: List[Contributor], lowest: Contributor): Contributor = _topTen match {
      case Nil => lowest
      case c::rest => if (c.getAppearances() <= lowest.getAppearances()) _findLowest(rest, c) else _findLowest(rest, lowest)
    }
    topTen.indexOf(_findLowest(topTen, topTen.head))
  }
}// end of reducer class

