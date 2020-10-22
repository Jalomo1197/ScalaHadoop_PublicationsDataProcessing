import javax.xml.parsers.SAXParser
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io
import org.apache.hadoop.io.{IntWritable, LongWritable, MapWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.apache.hadoop.mapreduce.{Job, Mapper, Reducer}
//import org.apache.mahout.text.wikipedia.XmlInputFormat

import scala.collection.convert.ImplicitConversions.{`iterable AsScalaIterable`, `iterable asJava`}
import scala.collection.mutable
import scala.xml.XML

object Driver {
  def main(args: Array[String]): Unit = {
    val configuration = new Configuration
    configuration.set("xmlinput.start", "<article,<phdthesis,<incollection,<inproceedings,<proceedings,<book,<mastersthesis")
    configuration.set("xmlinput.end", "</article>,</phdthesis>,</incollection>,</inproceedings>,</proceedings>,</book>,</mastersthesis>")
    val job = Job.getInstance(configuration,"wordcount")
    job.setInputFormatClass(classOf[XmlInputFormatWithMultipleTags])
    job.setJarByClass(this.getClass)
    job.setMapperClass(classOf[ListRankingMapper])
    job.setReducerClass(classOf[ListRankingReducer])
    // output pair of mapper
    job.setMapOutputKeyClass(classOf[Text])
    job.setMapOutputValueClass(classOf[MapWritable])
    // output pair of reducer
    job.setOutputKeyClass(classOf[Text])
    job.setOutputValueClass(classOf[Text])
    FileInputFormat.addInputPath(job, new Path(args(0)))
    FileOutputFormat.setOutputPath(job, new Path(args(1)))
    System.exit(if(job.waitForCompletion(true))  0 else 1)
  }
}
