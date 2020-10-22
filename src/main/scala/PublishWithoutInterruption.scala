import org.apache.hadoop.io
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.{Mapper, Reducer}
import org.xml.sax.SAXParseException

import scala.xml.{Elem, XML}


// - MAPPER CLASS
class PublishWithoutInterruptionMapper extends Mapper[LongWritable, Text, Text, Text] {
  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, Text]#Context): Unit = {
    val XMLoption: Option[Elem] = XMLHandler.parseXML(value)    // Checking for valid input: XML section
    var XMLelem: Elem = null                                    // For xml element if it successfully was parsed
    if (XMLoption.isEmpty) return else XMLelem = XMLoption.get  // If None (meaning wasn't parsed) return
    val year = XMLHandler.getYear(XMLelem)                             // Year of publication
    val contributors = XMLHandler.getContributors(XMLelem)      // Editors or Authors
    for (c <- contributors) {
      context.write(new Text(c.text), new Text(year))
    }
  }
} // end of mapper class


// - REDUCER CLASS
class PublishWithoutInterruptionReducer extends Reducer[Text,Text,Text,Text] {
  override
  def reduce(key: Text, values: java.lang.Iterable[Text], context: Reducer[Text, Text, Text, Text]#Context): Unit = {
    var contributorPublishedYears: List[Int] = List()
    // Adding all publication years to list
    values.forEach(publishYearText => {
      val publishYearString = publishYearText.toString // year
      try {
        contributorPublishedYears =  (publishYearString.toInt) :: contributorPublishedYears
      }
      catch {
        case e: NumberFormatException => println("[WARN] publication year was not parsed")
      }
    })
    // sorting year list
    val sortedYearList = contributorPublishedYears.sorted
    // If list contains 10 consecutive years, then write
    val uninterruptedYears = withoutInterruption(sortedYearList)
    if (uninterruptedYears >= 10)
      context.write(key, new Text(", published for " + uninterruptedYears + " years without interruption" ))
  }// end of reduce function

  def withoutInterruption(years: List[Int]): Int = {
    var previous = -1
    var count = 0
    var maxCount = 0
    for (year <- years) {
      if (previous == -1) { // first element only
        previous = year
        count = 1
      }
      else if (previous == year - 1) { // valid continuation
        count += 1
        previous = year
      }
      else if (previous == year){ // same year, no side affect
        previous = year
      }
      else{ // Interruption, restart
        if(count > maxCount)
          maxCount = count
        count = 1
        previous = year
      }
      if(count > maxCount)
        maxCount = count
    }
    maxCount
  }

}// end of reducer class
