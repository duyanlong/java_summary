## spark thrift server 



低层是

```
spark-submit --class org.apache.spark.sql.hive.thriftserver.HiveThriftServer2 --name Thrift JDBC/ODBC Server
```

#### 关闭

```
/usr/local/service/spark/sbin/stop-thriftserver.sh 
```

#### 以固定资源启动 

```
/usr/local/service/spark/sbin/start-thriftserver.sh \
--hiveconf spark.yarn.queue=default \
--master yarn \
--deploy-mode client \
--driver-memory 2G \
--driver-cores 2 \
--executor-memory 4G \
--num-executors 15 \
--executor-cores 4 \
--conf spark.executor.memoryOverhead=2G \
--hiveconf hive.server2.thrift.bind.host=xx.xx.xxx.xx \
--hiveconf hive.server2.thrift.port=7001 \
--conf spark.hadoop.fs.hdfs.impl.disable.cache=true 
```

说明spark.hadoop.fs.hdfs.impl.disable.cache=true 

```
关闭HDFS文件系统的缓存机制，如果不设置并发提交sql到spark thrift server可能会出现java.io.IOException: Filesystem closed
参考
https://blog.csdn.net/helloxiaozhe/article/details/113367654
http://stackoverflow.com/questions/23779186/ioexception-filesystem-closed-exception-when-running-oozie-workflow
http://stackoverflow.com/questions/20057881/hadoop-filesystem-closed-exception-when-doing-bufferedreader-close
```

```
默认端口与hive-site.xml hiveserver2端口一致也可由配置指定
--hiveconf hive.server2.thrift.bind.host=xx.xx.xxx.xx 
--hiveconf hive.server2.thrift.port=7001 
```



#### 开启动态资源分配

参考文档

https://cloud.tencent.com/document/product/589/38242

将spark-<version>-yarn-shuffle.jar` 拷贝到 `/usr/local/service/hadoop/share/hadoop/yarn/lib

```
cp /usr/local/service/spark/yarn/spark-3.1.2-yarn-shuffle.jar /usr/local/service/hadoop/share/hadoop/yarn/lib
```

修改hadoop配置文件yarn-site.xml

```
<!--修改-->
<property>
<name>yarn.nodemanager.aux-services</name>
<value>mapreduce_shuffle,spark_shuffle</value>
</property>

<!--添加-->
<property>
<name>yarn.nodemanager.aux-services.spark_shuffle.class</name>
<value>org.apache.spark.network.yarn.YarnShuffleService</value>
</property>
<property>
<name>spark.yarn.shuffle.stopOnFailure</name>
<value>false</value>
</property>

```

分发yarn-site.xml到各个resourceManager节点，重启yarn

```
/usr/local/service/spark/sbin/start-thriftserver.sh \
--hiveconf spark.yarn.queue=default \
--master yarn \
--deploy-mode client \
--driver-memory 2G \
--driver-cores 2 \
--executor-memory 4G \
--executor-cores 4 \
--conf spark.executor.memoryOverhead=2G \
--conf spark.hadoop.fs.hdfs.impl.disable.cache=true \
--conf spark.dynamicAllocation.enabled=true \
--conf spark.shuffle.service.enabled=true \
--conf spark.dynamicAllocation.initialExecutors=2 \
--conf spark.dynamicAllocation.minExecutors=1 \
--conf spark.dynamicAllocation.maxExecutors=15 \
--conf spark.dynamicAllocation.schedulerBacklogTimeout=1s \
--conf spark.dynamicAllocation.sustainedSchedulerBacklogTimeout=5s \
--conf spark.dynamicAllocation.executorIdleTimeout=60s 
```

说明

```
spark.shuffle.service.enabled 	true 	启动 shuffle 服务。
spark.dynamicAllocation.enabled 	true 	启动动态资源分配。
spark.dynamicAllocation.minExecutors 	1 	每个 Application 最小分配的 executor 数。
spark.dynamicAllocation.maxExecutors 	30 	每个 Application 最大分配的 executor 数。
spark.dynamicAllocation.initialExecutors 	1 	一般情况下与 spark.dynamicAllocation.minExecutors 值相同。
spark.dynamicAllocation.schedulerBacklogTimeout 	1s 	已有挂起的任务积压超过此持续事件，则将请求新的执行程序。
spark.dynamicAllocation.sustainedSchedulerBacklogTimeout 	5s 	带处理任务队列依然存在，则此后每隔几秒再次出发，每轮请求的 executor 数目与上轮相比呈指数增长。
spark.dynamicAllocation.executorIdleTimeout 	60s 	Application 在空闲超过几秒钟时会删除 executor
```

#### spark task调度

spark默认FIFO调度 spark.scheduler.mode=FIFO

详细介绍

https://www.jianshu.com/p/fdc848bc32be

https://blog.51cto.com/u_15278282/2931979

```
FIFO: 先进先出，优先级比较算法如下，

    1.比较priority，小的优先；
    2.priority相同则比较StageId，小的优先。

FAIR：公平调度，优先级比较算法如下，

    1.runningTasks小于minShare的优先级比不小于的优先级要高。
    2.若两者运行的runningTasks都比minShare小，则比较minShare使用率(runningTasks/max(minShare,1))，使用率越低优先级越高。
    3.若两者的minShare使用率相同，则比较权重使用率(runningTasks/weight)，使用率越低优先级越高。
    4.若权重也相同，则比较name，小的优先
```



以FAIR调度模式启动 spark.scheduler.mode=FAIR

```
/usr/local/service/spark/sbin/start-thriftserver.sh \
--hiveconf spark.yarn.queue=default \
--master yarn \
--deploy-mode client \
--driver-memory 2G \
--driver-cores 2 \
--executor-memory 4G \
--executor-cores 4 \
--conf spark.executor.memoryOverhead=2G \
--conf spark.hadoop.fs.hdfs.impl.disable.cache=true \
--conf spark.dynamicAllocation.enabled=true \
--conf spark.shuffle.service.enabled=true \
--conf spark.dynamicAllocation.initialExecutors=2 \
--conf spark.dynamicAllocation.minExecutors=1 \
--conf spark.dynamicAllocation.maxExecutors=15 \
--conf spark.dynamicAllocation.schedulerBacklogTimeout=1s \
--conf spark.dynamicAllocation.sustainedSchedulerBacklogTimeout=5s \
--conf spark.dynamicAllocation.executorIdleTimeout=60s \
--conf spark.scheduler.mode=FAIR \
--conf spark.scheduler.allocation.file=/usr/local/service/spark/conf/fairscheduler.xml.test
```

Fair Scheduler Pool的划分依赖于配置文件fairscheduler.xml，默认的配置文件为'fairscheduler.xml'，也可以通过配置项"spark.scheduler.allocation.file"指定配置文件

fairscheduler.xml.test

```

<allocations>
  <pool name="default">
    <schedulingMode>FAIR</schedulingMode>
    <weight>1</weight>
    <minShare>3</minShare>
  </pool>
  <pool name="test">
    <schedulingMode>FIFO</schedulingMode>
    <weight>2</weight>
    <minShare>5</minShare>
  </pool>
</allocations>
```



```
name: 该调度池的名称，可根据该参数使用指定pool sc.setLocalProperty("spark.scheduler.pool", "test")
weight: 该调度池的权重，各调度池根据该参数分配系统资源。每个调度池得到的资源数为weight / sum(weight)，weight为2的分配到的资源为weight为1的两倍。
minShare: 该调度池需要的最小资源数（CPU核数）。fair调度器首先会尝试为每个调度池分配最少minShare资源，然后剩余资源才会按照weight大小继续分配。
schedulingMode: 该调度池内的调度模式
```

在不同的作业中指定到不同的调度池 

```
SET spark.sql.thriftserver.scheduler.pool=default
SET spark.sql.thriftserver.scheduler.pool=test
```

