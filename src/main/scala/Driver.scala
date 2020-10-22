import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{IntWritable, LongWritable, MapWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.apache.hadoop.mapreduce.{Job, Mapper, Reducer}

object Driver {
  def main(args: Array[String]): Unit = {
    val configuration = new Configuration
    configuration.set("xmlinput.start", "<article,<phdthesis,<incollection,<inproceedings,<proceedings,<book,<mastersthesis")
    configuration.set("xmlinput.end", "</article>,</phdthesis>,</incollection>,</inproceedings>,</proceedings>,</book>,</mastersthesis>")
    val job = Job.getInstance(configuration,"wordcount")
    job.setInputFormatClass(classOf[XmlInputFormatWithMultipleTags])
    job.setJarByClass(this.getClass)
    // Specifying which functionality
    job.setMapperClass(classOf[ListRankingMapper])
    job.setReducerClass(classOf[ListRankingReducer])
    // Output pair of mapper
    job.setMapOutputKeyClass(classOf[Text])
    job.setMapOutputValueClass(classOf[MapWritable])
    // Output pair of reducer
    job.setOutputKeyClass(classOf[Text])
    job.setOutputValueClass(classOf[Text])
    // Input/Output paths from command "hadoop jar <.jar file> <MainClass> <XMlInputPath> <OutputPath>
    FileInputFormat.addInputPath(job, new Path(args(0)))
    FileOutputFormat.setOutputPath(job, new Path(args(1)))
    System.exit(if(job.waitForCompletion(true))  0 else 1)
  }
}
