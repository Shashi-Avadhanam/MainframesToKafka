# Mainframes to kafka

This repo is a solution prototype written in scala to read mainframe cobol fixed length copybook files;convert each record into Json and load into Kafka.It takes schema from Cobol copybook. It uses JRecord library for reading mainframes data; jackson scala module for json conversion.

### Set up
Jrecord libraries are not available in Maven. Please install them as shown below before mvn package
```
mvn install:install-file -Dfile=lib/JRecord.jar -DgroupId=net.sf.JRecord -DartifactId=JRecord -Dversion=0.90 -Dpackaging=jar
mvn install:install-file -Dfile=lib/cb2xml.jar -DgroupId=net.sf.cb2xml -DartifactId=cb2xml -Dversion=1.0 -Dpackaging=jar
```

Install kafka,start ZK and Kafka servers and create below topic for example data provided with this repo
```
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic User
```

### How to run
Below is a sample to run.Please check the User.cbl and User.dat in the data folder.
```
java -jar target/MainframetoKafka-0.0.1.jar data/User.cbl data/User.dat data/User.out
```

Check records are loaded into kafka topic. Records are also loaded into data/User.out for validation
```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic User --from-beginning
```