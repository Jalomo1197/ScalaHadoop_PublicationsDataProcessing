import org.apache.hadoop.fs.FSDataInputStream
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.DataOutputBuffer
import org.apache.hadoop.io.LongWritable
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.InputSplit
import org.apache.hadoop.mapreduce.RecordReader
import org.apache.hadoop.mapreduce.TaskAttemptContext
import org.apache.hadoop.mapreduce.lib.input.FileSplit
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import java.io.IOException
import java.nio.charset.StandardCharsets


/**
 * An implementation of XML input format.
 * Supports multiple start and end tags sent from the configuration.
 * start tags and end tags are delimited by ,(comma)
 */
object XmlInputFormatWithMultipleTags {
  val START_TAG_KEYS = "xmlinput.start"
  val END_TAG_KEYS = "xmlinput.end"

  class XmlRecordReader extends RecordReader[LongWritable, Text] {
    private var startTags : Array[Array[Byte]] = null
    private var endTags: Array[Array[Byte]] = null
    private var start: Long = 0L
    private var `end`: Long = 0L
    private var fsin: FSDataInputStream = null
    private val buffer: DataOutputBuffer = new DataOutputBuffer
    private val key: LongWritable = new LongWritable
    private val value: Text = new Text

    @throws[IOException]
    @throws[InterruptedException]
    override def initialize(is: InputSplit, tac: TaskAttemptContext): Unit = {
      val fileSplit: FileSplit = is.asInstanceOf[FileSplit]                     //downcasting to FileSplit
      val sTags: Array[String] = tac.getConfiguration.get(START_TAG_KEYS).split(",")   // Input beginning tags
      val eTags: Array[String] = tac.getConfiguration.get(END_TAG_KEYS).split(",")     // Input beginning tags
      startTags = new Array[Array[Byte]](sTags.length)
      endTags = new Array[Array[Byte]](sTags.length)
      for (i <- 0 until sTags.length) {
        startTags(i) = sTags(i).getBytes(StandardCharsets.UTF_8)
        endTags(i) = eTags(i).getBytes(StandardCharsets.UTF_8)
      }
      start = fileSplit.getStart
      `end` = start + fileSplit.getLength
      val file: Path = fileSplit.getPath
      val fs: FileSystem = file.getFileSystem(tac.getConfiguration)
      fsin = fs.open(fileSplit.getPath)
      fsin.seek(start)
    }

    @throws[IOException]
    @throws[InterruptedException]
    override def nextKeyValue: Boolean = {
      if (fsin.getPos < `end`) { //perform readuntillmatch for any of the specified tag
        val res = readUntilMatch(startTags, false)
        if (res != -1) { // Read until start_tag1 or start_tag2
          try {
            buffer.write(startTags(res - 1))
            //read all the contents before the end tag
            val res1 = readUntilMatch(endTags, true)
            if (res1 != -1) { // updating the buffer with contents between start and end tags.
              value.set(buffer.getData, 0, buffer.getLength)
              key.set(fsin.getPos)
              return true
            }
          } finally buffer.reset
        }
      }
      false
    }

    @throws[IOException]
    @throws[InterruptedException]
    override def getCurrentKey: LongWritable = key

    @throws[IOException]
    @throws[InterruptedException]
    override def getCurrentValue: Text = value

    @throws[IOException]
    @throws[InterruptedException]
    override def getProgress: Float = (fsin.getPos - start) / (`end` - start).toFloat

    @throws[IOException]
    override def close(): Unit = {
      if (fsin != null) fsin.close()
    }

    @throws[IOException]
    private def readUntilMatch(`match`: Array[Array[Byte]], withinBlock: Boolean): Int = {
      val tagPointers = new Array[Int](`match`.length)
      while (true){
        val currentByte : Int = fsin.read
        // end of file:
        if (currentByte == -1) return -1
        // save to buffer:
        if (withinBlock) buffer.write(currentByte)
        // check if we're matching any of the tag specified:
        for (i <- 0 until tagPointers.length) {
          if (currentByte == `match`(i)(tagPointers(i))) {
            tagPointers(i) += 1
            if (tagPointers(i) >= `match`(i).length) return i + 1
          }
          else tagPointers(i) = 0
        }
        // see if we've passed the stop point:
        if (!withinBlock && checkAllPointers(tagPointers) && fsin.getPos >= `end`) return -1
      }

      0 // Needed for scala to compile, Never be reached
    }

    private def checkAllPointers(tagPointers: Array[Int]): Boolean = {
      for (i <- tagPointers) {
        if (i != 0) return false
      }
      true
    }
  }

}

class XmlInputFormatWithMultipleTags extends TextInputFormat {
  override def createRecordReader(is: InputSplit, tac: TaskAttemptContext): RecordReader[LongWritable, Text] = new XmlInputFormatWithMultipleTags.XmlRecordReader
}