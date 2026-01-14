## livy
apache spark提供的两种基于命令行的处理交互方式虽然足够灵活，但在企业应用中面临诸如部署、安全等问题；为此产生了apache livy架构，一种基于apache spark的REST服务，
它不仅以Rest的方式代替了Spark传统的处理交互方式，同时也提供了企业应用中不可忽视的多用户，安全，以及容错的支持；

单点故障：首先将资源的使用和故障发生的可能性集中到了这些Gateway节点。由于所有的Spark进程都是在Gateway节点上启动的，这势必会增加Gateway节点的资源使用负担和故障发生的可能性，同时Gateway节点的故障会带来单点问题，造成Spark程序的失败。

平台化管理：其次难以管理、审计以及与已有的权限管理工具的集成。由于Spark采用脚本的方式启动应用程序，因此相比于Web方式少了许多管理、审计的便利性，同时也难以与已有的工具结合，如Apache Knox。

底层直接暴露给用户：同时也将Gateway节点上的部署细节以及配置不可避免地暴露给了登陆用户。 
为了避免上述这些问题，同时提供原生Spark已有的处理交互方式，并且为Spark带来其所缺乏的企业级管理、部署和审计功能，本文将介绍一个新的基于Spark的REST服务：Livy。

Livy结合了spark job server和Zeppelin的优点，并解决了spark job server和Zeppelin的缺点。

1. 支持jar和snippet code
2. 支持SparkContext和Job的管理
3. 支持不同SparkContext运行在不同进程，同一个进程只能运行一个SparkContext
4. 支持Yarn cluster模式
5. 提供restful接口，暴露SparkConte

* 什么是Livy
* Livy的特点
* Livy的运作流程阐述
* Livy的安装、启动、访问

## 什么是Livy
Livy是cloudera开发的通过Rest（注1）来连接、管理spark的解决方案

设计到一些角色：
1、客户端：browser.app终端设备
2、Livy Server
3、Livy Server获得用户的请求后，然后将job提交给spark集群执行

注1：
Rest即表述性状态传递（Representational State Transfer,简称REST）是Roy Fielding博士在2000年她的博士论文中提出来的一种软件架构风格，它是一种针对网络应用的设计，
目前在三中主流的web服务实现方案中，因为REST模式的Web服务与服装的SOAP和XML-RPC对比来讲明显的更加简洁，越来越多的web服务开始采用REST风格设计和实现，例如：

## Livy的特点
1、从任何地方提交job
2、使用交互式的java、scala、python语言与远程的spark集群进行通信
3、无需更改代码

## Livy的运作流程阐述
client<->rest server <->cluster manager(
driver(SparkContext) <-> executor*n
driver(SparkContext) <-> executor
)


## Livy的
### 安装
1、下载Livy
Livy安装可以从github上自己build，也可以直接从livy.io上直接下载tar包
2、解压后，在livy-env.sh中添加
export SPARK_HOME
export HADOOP_CONF_DIR
此外：也可以配置一些环节变量，如HADOOP_USER_NAME等
3、在livy.conf中可以进行一些配置
livy.spark.deploy-mode=client
livy.repl.enableHiveContext=true 是否启用对hive的支持
livy.impersonation.enabled=true 开启用户代理
livy.server.session.timeout=1h 设置session空闲过期时间
此外，其他的配置项目，可以跟进你的需要来配置，包括host和port等等

### livy启动
启动zk、hadoop集群
livy-server start

### 访问
1、通过livy-session，可以通过rest来执行spark-shell，用于处理交互式的请求。
```text
curl -XPOST --data '{"kind":"spark"}' -H "Content-Type:applicaiton/json" http**:8998/sessions
```

通过8998页面查看session实例
```text
curl -XPOST http://**:8998/sessions/2/statements -H 'Content-Type:application/json' -d '{"code":"sc.textFile(\"hdfs://***\").flatMap(_.split(\" \")).map((_,1)).reduceByKey(_+_).saveAsTextFile(\"***\")"}'
```
执行上述命令可实现基于livy session的spark code执行

2、可以使用livy-batches通过rest来执行spark-submit非交互式请求
```text
curl -XPOST http://**:8998/batches -H 'Content-Type:application/json' 
-d '{"conf***"}'
```
