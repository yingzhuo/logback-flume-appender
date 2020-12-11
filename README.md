[![License](http://img.shields.io/badge/License-Apache_2-red.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![JDK](http://img.shields.io/badge/JDK-v8.0-yellow.svg)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
[![Build](http://img.shields.io/badge/Build-Maven_2-green.svg)](https://maven.apache.org/)

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
    <version>0.0.1</version>
</dependency>
```

### 用法

```xml
<appender name="flume" class="com.github.yingzhuo.logback.flume.FlumeLogstashV1Appender">
    <flumeAgents>
        10.211.55.3:4141
    </flumeAgents>
    <flumeProperties>
        connect-timeout=4000;
        request-timeout=8000
    </flumeProperties>
    <batchSize>100</batchSize>
    <reportingWindow>1000</reportingWindow>
    <additionalAvroHeaders>
        myHeader = myValue
    </additionalAvroHeaders>
    <application>My Application</application>
    <layout class="ch.qos.logback.classic.PatternLayout">
        <pattern>%d{HH:mm:ss.SSS} %-5level %logger{36} - \(%file:%line\) - %message%n%ex</pattern>
    </layout>
</appender>
```
