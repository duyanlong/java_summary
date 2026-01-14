## jobserver启动异常java.lang.annotation.AnnotationFormatError
* 错误信息：
```text
2021-04-12 20:52:42.022 ERROR 2453 --- [           main] o.s.boot.SpringApplication               : Application run failed

java.lang.annotation.AnnotationFormatError: Invalid default: public abstract java.lang.Class org.mybatis.spring.annotation.MapperScan.factoryBean()
	at java.lang.reflect.Method.getDefaultValue(Method.java:612) ~[na:1.8.0_271]
	at sun.reflect.annotation.AnnotationType.<init>(AnnotationType.java:132) ~[na:1.8.0_271]
	at sun.reflect.annotation.AnnotationType.getInstance(AnnotationType.java:85) ~[na:1.8.0_271]
	at sun.reflect.annotation.AnnotationParser.parseAnnotation2(AnnotationParser.java:266) ~[na:1.8.0_271]
	at sun.reflect.annotation.AnnotationParser.parseAnnotations2(AnnotationParser.java:120) ~[na:1.8.0_271]
	at sun.reflect.annotation.AnnotationParser.parseAnnotations(AnnotationParser.java:72) ~[na:1.8.0_271]
	at java.lang.Class.createAnnotationData(Class.java:3521) ~[na:1.8.0_271]
	at java.lang.Class.annotationData(Class.java:3510) ~[na:1.8.0_271]
	at java.lang.Class.getAnnotations(Class.java:3446) ~[na:1.8.0_271]
	at org.springframework.core.type.StandardAnnotationMetadata.<init>(StandardAnnotationMetadata.java:70) ~[spring-core-5.0.8.RELEASE.jar:5.0.8.RELEASE]
	at org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition.<init>(AnnotatedGenericBeanDefinition.java:58) ~[spring-beans-5.0.8.RELEASE.jar:5.0.8.RELEASE]
	at org.springframework.context.annotation.AnnotatedBeanDefinitionReader.doRegisterBean(AnnotatedBeanDefinitionReader.java:216) ~[spring-context-5.0.8.RELEASE.jar:5.0.8.RELEASE]
	at org.springframework.context.annotation.AnnotatedBeanDefinitionReader.registerBean(AnnotatedBeanDefinitionReader.java:145) ~[spring-context-5.0.8.RELEASE.jar:5.0.8.RELEASE]
	at org.springframework.context.annotation.AnnotatedBeanDefinitionReader.register(AnnotatedBeanDefinitionReader.java:135) ~[spring-context-5.0.8.RELEASE.jar:5.0.8.RELEASE]
	at org.springframework.boot.BeanDefinitionLoader.load(BeanDefinitionLoader.java:158) ~[spring-boot-2.0.4.RELEASE.jar:2.0.4.RELEASE]
	at org.springframework.boot.BeanDefinitionLoader.load(BeanDefinitionLoader.java:135) ~[spring-boot-2.0.4.RELEASE.jar:2.0.4.RELEASE]
	at org.springframework.boot.BeanDefinitionLoader.load(BeanDefinitionLoader.java:127) ~[spring-boot-2.0.4.RELEASE.jar:2.0.4.RELEASE]
	at org.springframework.boot.SpringApplication.load(SpringApplication.java:704) [spring-boot-2.0.4.RELEASE.jar:2.0.4.RELEASE]
	at org.springframework.boot.SpringApplication.prepareContext(SpringApplication.java:393) [spring-boot-2.0.4.RELEASE.jar:2.0.4.RELEASE]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:328) [spring-boot-2.0.4.RELEASE.jar:2.0.4.RELEASE]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1258) [spring-boot-2.0.4.RELEASE.jar:2.0.4.RELEASE]
	at org.springframework.boot.SpringApplication.run(SpringApplication.java:1246) [spring-boot-2.0.4.RELEASE.jar:2.0.4.RELEASE]
	at com.tme.it.jobserver.Application.main(Application.java:13) [classes/:na]
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.8.0_271]
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[na:1.8.0_271]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:1.8.0_271]
	at java.lang.reflect.Method.invoke(Method.java:498) ~[na:1.8.0_271]
	at com.intellij.rt.execution.CommandLineWrapper.main(CommandLineWrapper.java:63) [idea_rt.jar:na]
```
* 解决方案：
```text
pom.xml中加入spring-jdbc依赖包

            <dependency>
                <groupId>org.springframework</groupId>
                <artifactId>spring-jdbc</artifactId>
                <version>5.0.8.RELEASE</version>
            </dependency>
```


## clickhouse 因为spark写数据量大或负载加高，通信超时 ru.yandex.clickhouse.except.ClickHouseException: ClickHouse exception, code: 159, host: xx.xx.xxx.xx, port: 8123; Read timed out
* 错误信息
ru.yandex.clickhouse.except.ClickHouseException: ClickHouse exception, code: 159, host: xx.xx.xxx.xx, port: 8123; Read timed out
at ru.yandex.clickhouse.except.ClickHouseExceptionSpecifier.getException(ClickHouseExceptionSpecifier.java:85)
at ru.yandex.clickhouse.except.ClickHouseExceptionSpecifier.specify(ClickHouseExceptionSpecifier.java:55)
at ru.yandex.clickhouse.except.ClickHouseExceptionSpecifier.specify(ClickHouseExceptionSpecifier.java:24)
at ru.yandex.clickhouse.ClickHouseStatementImpl.sendStream(ClickHouseStatementImpl.java:1026)
at ru.yandex.clickhouse.ClickHouseStatementImpl.sendStream(ClickHouseStatementImpl.java:985)
at ru.yandex.clickhouse.ClickHouseStatementImpl.sendStream(ClickHouseStatementImpl.java:978)
at ru.yandex.clickhouse.ClickHousePreparedStatementImpl.executeBatch(ClickHousePreparedStatementImpl.java:372)
at ru.yandex.clickhouse.ClickHousePreparedStatementImpl.executeBatch(ClickHousePreparedStatementImpl.java:349)
at org.apache.spark.sql.execution.datasources.jdbc.JdbcUtils$.savePartition(JdbcUtils.scala:687)
at org.apache.spark.sql.execution.datasources.jdbc.JdbcUtils$.$anonfun$saveTable$1(JdbcUtils.scala:856)
at org.apache.spark.sql.execution.datasources.jdbc.JdbcUtils$.$anonfun$saveTable$1$adapted(JdbcUtils.scala:854)
at org.apache.spark.rdd.RDD.$anonfun$foreachPartition$2(RDD.scala:1020)
at org.apache.spark.rdd.RDD.$anonfun$foreachPartition$2$adapted(RDD.scala:1020)
at org.apache.spark.SparkContext.$anonfun$runJob$5(SparkContext.scala:2236)
at org.apache.spark.scheduler.ResultTask.runTask(ResultTask.scala:90)
at org.apache.spark.scheduler.Task.run(Task.scala:131)
at org.apache.spark.executor.Executor$TaskRunner.$anonfun$run$3(Executor.scala:497)
at org.apache.spark.util.Utils$.tryWithSafeFinally(Utils.scala:1439)
at org.apache.spark.executor.Executor$TaskRunner.run(Executor.scala:500)
at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
at java.lang.Thread.run(Thread.java:748)
Caused by: java.net.SocketTimeoutException: Read timed out
at java.net.SocketInputStream.socketRead0(Native Method)
at java.net.SocketInputStream.socketRead(SocketInputStream.java:116)
at java.net.SocketInputStream.read(SocketInputStream.java:171)
at java.net.SocketInputStream.read(SocketInputStream.java:141)
at org.apache.http.impl.io.SessionInputBufferImpl.streamRead(SessionInputBufferImpl.java:137)
at org.apache.http.impl.io.SessionInputBufferImpl.fillBuffer(SessionInputBufferImpl.java:153)
at org.apache.http.impl.io.SessionInputBufferImpl.readLine(SessionInputBufferImpl.java:280)

* 解决方案
在任务中writer部分加入超时时间配置  "params":{"socket_timeout":"600000"}, 