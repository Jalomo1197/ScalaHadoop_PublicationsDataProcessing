import org.apache.hadoop.io
import org.apache.hadoop.io.{IntWritable, LongWritable, MapWritable, Text}
import org.apache.hadoop.mapreduce.{Mapper, Reducer}
import org.xml.sax.SAXParseException

import scala.xml.{Elem, XML}


// - MAPPER CLASS
class ListRankingMapper extends Mapper[LongWritable, Text, Text, MapWritable] {

  override def map(key: LongWritable, value: Text, context: Mapper[LongWritable, Text, Text, MapWritable]#Context): Unit = {
    val XMLoption: Option[Elem] = XMLHandler.parseXML(value)    // Checking for valid input: XML section
    var XMLelem: Elem = null                                    // for xml element if it successfully was parsed
    if (XMLoption.isEmpty) return else XMLelem = XMLoption.get  // If None (meaning wasn't parsed) return
    val contributors = XMLHandler.getContributors(XMLelem)      // Editors or Authors
    val rankingMap: MapWritable = new MapWritable()
    val co_authors = contributors.length - 1
    if (co_authors > 0){
      contributors.foreach( c => {
        rankingMap.put(new Text(c.text), new IntWritable(co_authors))
      })
      context.write(new Text("Co-Author Ranking"), rankingMap)
    }
    else if (co_authors == 0){
      contributors.foreach( c => {
        rankingMap.put(new Text(c.text), new IntWritable(1))
      })
      context.write(new Text("Single Author Ranking"), rankingMap)
    }
  }
} // end of mapper class


// - REDUCER CLASS
class ListRankingReducer extends Reducer[Text,MapWritable,Text,Text] {
  override
  def reduce(key: Text, values: java.lang.Iterable[MapWritable], context: Reducer[Text, MapWritable, Text, Text]#Context): Unit = {
    // Map to store authors appearances in articles
    var contributorTotalScore: Map[String, Int] = Map()
    var scores : List[(String, Int)] = List()
    values.forEach( map => {
      map.forEach((name, amount) => {
        val contributor = name.asInstanceOf[Text].toString
        val score = amount.asInstanceOf[IntWritable].get()
        if (contributorTotalScore.contains(contributor))     // Updating map accordingly
          contributorTotalScore += (contributor -> (contributorTotalScore(contributor) + score))
        else
          contributorTotalScore += (contributor -> score)
      })
    })

    for ((name, score) <- contributorTotalScore) scores = (name, score) :: scores
    val sortedScores = scores.sortWith(_._2 > _._2)
    val listingLimit = if (100 > sortedScores.length) sortedScores.length else 100
    var listing: String = ""
    for (i <- 0 until listingLimit) listing += sortedScores(i)._1 + ":" + sortedScores(i)._2 + ", "
    context.write(key, new Text(listing))
  }
}// end of reducer class

