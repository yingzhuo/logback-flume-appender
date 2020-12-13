[![License](http://img.shields.io/badge/License-Apache_2-red.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![JDK](http://img.shields.io/badge/JDK-v8.0-yellow.svg)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
[![Build](http://img.shields.io/badge/Build-Maven_2-green.svg)](https://maven.apache.org/)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.yingzhuo/logback-flume-appender.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.yingzhuo%22%20AND%20a:%22logback-flume-appender%22)

# logback-flume-appender

这个东西不是我原创的。我只找到了如下这个jar包用于将logback产生的业务日志发送到flume，最终传递到hdfs/hive。

```xml
<dependency>
    <groupId>com.teambytes.logback</groupId>
    <artifactId>logback-flume-appender_2.11</artifactId>
    <version>0.0.9</version>
</dependency>
```

由于兼容性等原因，我做了一些工作:

* 原作者的scala语言的部分，我用java改写了。
* 升级flume-ng-sdk到1.9.0版本。
* JDK的最低要求调整到1.8

**对于原作者，得罪了。**

改写后的jar包坐标：

```xml
<!-- logback-appender-for-flume -->
<dependency>
    <groupId>com.github.yingzhuo</groupId>
    <artifactId>logback-flume-appender</artifactId>
    <version>0.2.0</version>
</dependency>
```

### 用法

1) flume agent配置

```config
###############################################################################
# Basic
###############################################################################
myagent.sources = mysource
myagent.channels = mychannel
myagent.sinks = mysink

###############################################################################
# Source(s)
###############################################################################
myagent.sources.mysource.type = avro
myagent.sources.mysource.bind = 0.0.0.0
myagent.sources.mysource.port = 4141

myagent.sources.mysource.selector.type = replicating

###############################################################################
# Channel(s)
###############################################################################

# kafka 实现
myagent.channels.mychannel.type = org.apache.flume.channel.kafka.KafkaChannel
myagent.channels.mychannel.kafka.bootstrap.servers = 10.211.55.3:9092
myagent.channels.mychannel.kafka.topic = flume-channel
myagent.channels.mychannel.kafka.group.id = flume

###############################################################################
# Sink(s)
###############################################################################
myagent.sinks.mysink.type = hdfs
myagent.sinks.mysink.hdfs.path = hdfs://localhost:9000/flume/%{application}/%{type}/%Y-%m-%d
myagent.sinks.mysink.hdfs.useLocalTimeStamp = true
myagent.sinks.mysink.hdfs.fileType = DataStream
myagent.sinks.mysink.hdfs.writeFormat = Text
myagent.sinks.mysink.hdfs.round = true
myagent.sinks.mysink.hdfs.rollInterval = 0
myagent.sinks.mysink.hdfs.rollSize = 134217700
myagent.sinks.mysink.hdfs.rollCount= 0

myagent.sinks.mysink.type = logger

###############################################################################
# Assemble
###############################################################################
myagent.sources.mysource.channels = mychannel
myagent.sinks.mysink.channel = mychannel
```

2) logback配置 (片段)

```xml
<appender name="FLUME" class="com.github.yingzhuo.logback.flume.FlumeAvroAppender">
    <flumeAgents>
        10.211.55.3:4141
    </flumeAgents>
    <flumeProperties>
        connect-timeout=4000;
        request-timeout=8000
    </flumeProperties>
    <batchSize>100</batchSize>
    <reportingWindow>1000</reportingWindow>
    <avroHeaders>
        <application>playground</application>
        <tier>playground</tier>
        <type>flume</type>
    </avroHeaders>
    <!--
    <additionalAvroHeaders>
        key1 = value1;
        key2 = value2
    </additionalAvroHeaders>
    -->
    <layout class="ch.qos.logback.classic.PatternLayout">
        <pattern>%message%n%ex</pattern>
    </layout>
</appender>
```

### 许可证

[Apache License](LICENSE)
