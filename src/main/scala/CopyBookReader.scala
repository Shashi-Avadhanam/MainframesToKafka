import java.io.{BufferedWriter, FileWriter}
import net.sf.JRecord.Common.Constants
import net.sf.JRecord.Details.AbstractLine
import net.sf.JRecord.External.{CopybookLoader, ExternalRecord}
import net.sf.JRecord.IO.CobolIoProvider
import net.sf.JRecord.Numeric.Convert
import net.sf.JRecord.Details.LayoutDetail
import net.sf.JRecord.External.CopybookLoaderFactory
import net.sf.JRecord.External.Def.ExternalField
import collection.JavaConversions._
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging

object CopyBookReader extends LazyLogging {

  def main(args: Array[String]): Unit = {

    if (args.length != 3) {
      println("Arguements: Copybook Inputdatafile Outputdatafile ")
      sys.exit()
    }

    val copyBook = args(0)
    val dataFile = args(1)
    val outputFile = args(2)
    val kafkaTopic=copyBook.split('/').last.split('.')(0)
    logger.info(s"Using cobol copyBook: ${copyBook}")
    logger.info(s"Using cobol data file: ${dataFile}")
    logger.info(s"Using output file : ${outputFile}")
    logger.info(s"Using Kafka topic: ${kafkaTopic}")
    var lineNum = 0
    val fileStructure: Int = Constants.IO_FIXED_LENGTH
    var mainframeRecord: AbstractLine = null
    val defaultConfig = ConfigFactory.parseResources("kafka.conf")
    try {
      val iob = CobolIoProvider.getInstance()
        .newIOBuilder(copyBook)
        .setFont("cp037") // US EBCDIC
        .setFileOrganization(Constants.IO_FIXED_LENGTH_RECORDS)
      val reader = iob.newReader(dataFile)
      val writer = new BufferedWriter(new FileWriter(outputFile))
      val loader = CopybookLoaderFactory.getInstance.getLoader(CopybookLoaderFactory.COBOL_LOADER)
      val externalLayout = loader.loadCopyBook(copyBook, 0, 0, "", 0, 0,null)
      val layout = externalLayout.asLayoutDetail
      val kafkaProducer =  KafkaLoader.setConfig(defaultConfig)
      while ( {
        mainframeRecord = reader.read;
        mainframeRecord!= null
      }) {
        lineNum += 1

        var i=0
        /*for (field <- externalLayout.getRecordFields) {
          writer.append(field.getName()+ ":" + mainframeRecord.getFieldValue(0, i).asString + "\t")
          i=i+1
        }*/
        var mainframeRecordMap:Map[String, String]=Map()
        for ((str,fld) <- mainframeRecord.getLayout.getFieldNameMap ) {
          mainframeRecordMap+=(fld.getName -> mainframeRecord.getFieldValue(fld.getName).asString)
          writer.write(fld.getName+":"+ mainframeRecord.getFieldValue(fld.getName).asString)

        }
        logger.info("Extracted record from mainframes")
        logger.info(mainframeRecord.getFieldValue("USER-ID").asString()+","+mainframeRecord.getFieldValue("USER-NAME").asString()+","+mainframeRecord.getFieldValue("USER-AGE").asString()+","+mainframeRecord.getFieldValue("CM00-LAST-FOUR-SSN").asString()+","+mainframeRecord.getFieldValue("CM00-RANDOM-NUM").asString()+","+mainframeRecord.getFieldValue("CM00-RANDOM-STRING").asString())
        val json= JsonUtil.toJson(mainframeRecordMap)
        KafkaLoader.loadintoKafka(kafkaProducer,kafkaTopic,json)
        writer.newLine

      }
      reader.close()
      writer.close()
    } catch {
      case e: Exception => {
        println("~~> " + lineNum + " " + e.getMessage)
        println()
        e.printStackTrace()
      }
    }
  }
}