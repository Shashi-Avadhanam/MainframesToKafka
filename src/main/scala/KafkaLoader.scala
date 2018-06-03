
import java.util.Properties


import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer._

object KafkaLoader extends LazyLogging{


  def setConfig(config: Config): KafkaProducer[AnyRef, AnyRef] = {
    val props: Properties = new Properties()
    props.put("metadata.broker.list", config.getString("brokerList"))
    props.put("bootstrap.servers", config.getString("bootstrapservers"))
    props.put("key.serializer",config.getString("keyserializer") )
    props.put("value.serializer",config.getString("valueserializer"))
    new KafkaProducer(props)
  }


def loadintoKafka(producer: KafkaProducer[AnyRef, AnyRef],topic:String,message:String): Unit = {

  val record = new ProducerRecord[AnyRef, AnyRef](topic, message)
  logger.info("putting record into kafka")
  logger.info(topic+":"+message)
  producer.send(record)


}

}
