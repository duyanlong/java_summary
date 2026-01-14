## Dolphinscheduler调研

#### 一、简介：

分布式去中心化，易扩展的可视化 DAG 工作流任务调度系统

主要特点：

以DAG图的方式将Task按照任务的依赖关系关联起来，可实时可视化监控任务的运行状态

支持丰富的任务类型：Shell、MR、Spark、Flink、SQL(mysql、postgresql、hive、sparksql)、Python、Http、Sub_Process、Procedure等

支持工作流定时调度、依赖调度、手动调度、手动暂停/停止/恢复，同时支持失败重试/告警、从指定节点恢复失败、Kill任务等操作

支持工作流优先级、任务优先级及任务的故障转移及任务超时告警/失败

支持工作流全局参数及节点自定义参数设置

支持资源文件的在线上传/下载，管理等，支持在线文件创建、编辑

支持任务日志在线查看及滚动、在线下载日志等

实现集群HA，通过Zookeeper实现Master集群和Worker集群去中心化

支持对Master/Worker cpu load，memory，cpu在线查看

支持工作流运行历史树形/甘特图展示、支持任务状态统计、流程状态统计

支持补数

支持多租户

说明文档

```
https://dolphinscheduler.apache.org/zh-cn/docs/latest/user_doc/About_DolphinScheduler/About_DolphinScheduler.html
```



#### 二、部署：

获取源码包

```
wget https://mirrors.bfsu.edu.cn/apache/dolphinscheduler/2.0.3/apache-dolphinscheduler-2.0.3-src.tar.gz --no-check-certificate
```

获取安装包

```
wget https://mirrors.bfsu.edu.cn/apache/dolphinscheduler/2.0.3/apache-dolphinscheduler-2.0.3-bin.tar.gz --no-check-certificate
```

源码包解压路径xx.xx.xxx.xx /data/dolphinscheduler/apache-dolphinscheduler-2.0.3-src

编译

```
mvn -U clean package -Prelease -Dmaven.test.skip=true -Dmaven.javadoc.skip=true
```

编译完成安装包路径/data/dolphinscheduler/apache-dolphinscheduler-2.0.3-src/dolphinscheduler-dist/target/apache-dolphinscheduler-2.0.3-bin.tar.gz

解压tar -zxvf apache-dolphinscheduler-2.0.3-bin.tar.gz -C /data/dolphinscheduler/

创建部署路径mkdir /data/dolphinscheduler/dolphinscheduler-2.0.3

修改安装源文件夹路径名	mv /data/dolphinscheduler/apache-dolphinscheduler-2.0.3-bin install-dolphin

安装准备：

添加dolphin用户用于启动dolphin和dag实例执行，需要配置sudo权限和ssh免密登录

```
useradd dolphin
echo "dolphin" | passwd --stdin dolphin
chmod 640 /etc/sudoers
echo 'dolphin  ALL=(ALL)  NOPASSWD: NOPASSWD: ALL' >> /etc/sudoers
su dolphin
ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
```

安装依赖

```
安装psmisc
yum install psmisc -y
```

修改配置文件-伪分布式部署

vim /data/dolphinscheduler/install-dolphin/conf/config/install_config.conf

涉及改动部分

```
ips="xx.xx.xxx.xx"
sshPort="36000"
masters="xx.xx.xxx.xx"
workers="xx.xx.xxx.xx:default"
alertServer="xx.xx.xxx.xx"
apiServers="xx.xx.xxx.xx"
pythonGatewayServers="xx.xx.xxx.xx"
installPath="/data/dolphinscheduler/dolphinscheduler-2.0.3"
deployUser="dolphin"
dataBasedirPath="/data/dolphinscheduler/dolphinscheduler-2.0.3/tmp"
javaHome="/usr/local/jdk/"
apiServerPort="12345"
DATABASE_TYPE=${DATABASE_TYPE:-"mysql"}
SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL:-"jdbc:mysql://xx.xx.xxx.xx:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8"}
SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME:-"root"}
SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD:-"Kq0FYUSc42"}
registryPluginName="zookeeper"
registryServers="xx.xx.xxx.xx:2181"
registryNamespace="dolphinscheduler"
resourceStorageType="HDFS"
resourceUploadPath="/dolphinscheduler"
defaultFS="hdfs://xx.xx.xxx.xx:4007"
resourceManagerHttpAddressPort="5000"
singleYarnIp="xx.xx.xxx.xx"
hdfsRootUser="hadoop"
```

vim /data/dolphinscheduler/install-dolphin/conf/common.properties

```
data.basedir.path=/data/dolphinscheduler/dolphinscheduler-2.0.3/tmp
resource.storage.type=HDFS
resource.upload.path=/dolphinscheduler
hdfs.root.user=hadoop
fs.defaultFS=hdfs://xx.xx.xxx.xx:4007
resource.manager.httpaddress.port=5000
yarn.application.status.address=http://xx.xx.xxx.xx:%s/ws/v1/cluster/apps/%s
yarn.job.history.status.address=http://xx.xx.xxx.xx:5024/ws/v1/history/mapreduce/jobs/%s
```

vim /data/dolphinscheduler/install-dolphin/conf/env/dolphinscheduler_env.sh 

```
export HADOOP_HOME=/usr/local/service/hadoop
export HADOOP_CONF_DIR=/usr/local/service/hadoop/etc/hadoop
export SPARK_HOME1=/usr/local/service/spark
export SPARK_HOME2=/usr/local/service/spark
export PYTHON_HOME=/usr/bin/python
export JAVA_HOME=/usr/local/jdk
export HIVE_HOME=/usr/local/service/hive
export FLINK_HOME=/usr/local/service/flink
export DATAX_HOME=/data/calc_dev/datax

export PATH=$HADOOP_HOME/bin:$SPARK_HOME1/bin:$PYTHON_HOME/bin:$JAVA_HOME/bin:$HIVE_HOME/bin:$FLINK_HOME/bin:$DATAX_HOME/bin:$PATH
```

mysql驱动添加和配置修改

```
cp /usr/local/service/spark/jars/mysql-connector-java-8.0.27.jar /data/dolphinscheduler/install-dolphin/lib/
```

vim /data/dolphinscheduler/install-dolphin/conf/application-mysql.yaml 

```
driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://xx.xx.xxx.xx:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8
    username: root
    password: Kq0FYUSc42
```

vim /data/dolphinscheduler/install-dolphin/install.sh

```
datasourceDriverClassname="com.mysql.cj.jdbc.Driver"
```

初始化mysql数据库

```
sh /data/dolphinscheduler/install-dolphin/script/create-dolphinscheduler.sh
```

复制hadoop集群配置文件

```
cp /usr/local/service/hadoop/etc/hadoop/core-site.xml /data/dolphinscheduler/install-dolphin/conf/
cp /usr/local/service/hadoop/etc/hadoop/hdfs-site.xml /data/dolphinscheduler/install-dolphin/conf/
```

在hdfs创建dolphin上传目录

```
hdfs dfs -mkdir /dolphinscheduler
hdfs dfs -chmod 777 /dolphinscheduler
```

在zookeeper创建dolphinscheduler节点

```
sh /usr/local/service/zookeeper/bin/zkCli.sh -server xx.xx.xxx.xx:2181
create /dolphinscheduler
```

配置dolphin 安装路径读写权限 chown -R dolphin:dolphin /data/dolphinscheduler/

安装dolphinscheduler

```
cd /data/dolphinscheduler/install-dolphin/ && sh install.sh
```

查看服务启动

```
[dolphin@10 /data/dolphinscheduler/install-dolphin]$ jps
552664 LoggerServer
552872 PythonGatewayServer
553993 Jps
552591 WorkerServer
552737 AlertServer
552518 MasterServer
552807 ApiApplicationServer
```

登录web页面

```
http://xx.xx.xxx.xx:12345/dolphinscheduler/ui/view/login/index.html
账号密码
admin/dolphinscheduler123
```

#### 三、测试

登录web页面，测试图形化dag

测试shell，sql，datax，spark作业

dag导出为json文件

```
[
    {
        "processDefinition":{
            "id":2,
            "code":4652338640256,
            "name":"test",
            "version":42,
            "releaseState":"OFFLINE",
            "projectCode":4652156953088,
            "description":"",
            "globalParams":"[]",
            "globalParamList":[

            ],
            "globalParamMap":{

            },
            "createTime":"2022-02-25 16:13:16",
            "updateTime":"2022-03-03 00:56:04",
            "flag":"YES",
            "userId":2,
            "userName":null,
            "projectName":null,
            "locations":"[{\"taskCode\":4652334706048,\"x\":160,\"y\":74},{\"taskCode\":4652640021376,\"x\":530,\"y\":74},{\"taskCode\":4708134159488,\"x\":900,\"y\":74},{\"taskCode\":4708215982720,\"x\":1270,\"y\":74},{\"taskCode\":4708247313536,\"x\":1270,\"y\":272},{\"taskCode\":4711044355712,\"x\":1640,\"y\":74}]",
            "scheduleReleaseState":null,
            "timeout":0,
            "tenantId":2,
            "tenantCode":null,
            "modifyBy":null,
            "warningGroupId":0
        },
        "processTaskRelationList":[
            {
                "id":155,
                "name":"",
                "processDefinitionVersion":42,
                "projectCode":4652156953088,
                "processDefinitionCode":4652338640256,
                "preTaskCode":4652334706048,
                "preTaskVersion":13,
                "postTaskCode":4652640021376,
                "postTaskVersion":11,
                "conditionType":"NONE",
                "conditionParams":{

                },
                "createTime":"2022-03-03 00:56:04",
                "updateTime":"2022-03-03 00:56:04",
                "operator":2,
                "operateTime":"2022-03-03 00:56:04"
            },
            {
                "id":156,
                "name":"",
                "processDefinitionVersion":42,
                "projectCode":4652156953088,
                "processDefinitionCode":4652338640256,
                "preTaskCode":4708134159488,
                "preTaskVersion":5,
                "postTaskCode":4708215982720,
                "postTaskVersion":1,
                "conditionType":"NONE",
                "conditionParams":{

                },
                "createTime":"2022-03-03 00:56:04",
                "updateTime":"2022-03-03 00:56:04",
                "operator":2,
                "operateTime":"2022-03-03 00:56:04"
            },
            {
                "id":157,
                "name":"",
                "processDefinitionVersion":42,
                "projectCode":4652156953088,
                "processDefinitionCode":4652338640256,
                "preTaskCode":4708134159488,
                "preTaskVersion":5,
                "postTaskCode":4708247313536,
                "postTaskVersion":1,
                "conditionType":"NONE",
                "conditionParams":{

                },
                "createTime":"2022-03-03 00:56:04",
                "updateTime":"2022-03-03 00:56:04",
                "operator":2,
                "operateTime":"2022-03-03 00:56:04"
            },
            {
                "id":158,
                "name":"",
                "processDefinitionVersion":42,
                "projectCode":4652156953088,
                "processDefinitionCode":4652338640256,
                "preTaskCode":4652640021376,
                "preTaskVersion":11,
                "postTaskCode":4708134159488,
                "postTaskVersion":5,
                "conditionType":"NONE",
                "conditionParams":{

                },
                "createTime":"2022-03-03 00:56:04",
                "updateTime":"2022-03-03 00:56:04",
                "operator":2,
                "operateTime":"2022-03-03 00:56:04"
            },
            {
                "id":159,
                "name":"",
                "processDefinitionVersion":42,
                "projectCode":4652156953088,
                "processDefinitionCode":4652338640256,
                "preTaskCode":4708215982720,
                "preTaskVersion":1,
                "postTaskCode":4711044355712,
                "postTaskVersion":4,
                "conditionType":"NONE",
                "conditionParams":{

                },
                "createTime":"2022-03-03 00:56:04",
                "updateTime":"2022-03-03 00:56:04",
                "operator":2,
                "operateTime":"2022-03-03 00:56:04"
            },
            {
                "id":160,
                "name":"",
                "processDefinitionVersion":42,
                "projectCode":4652156953088,
                "processDefinitionCode":4652338640256,
                "preTaskCode":0,
                "preTaskVersion":0,
                "postTaskCode":4652334706048,
                "postTaskVersion":13,
                "conditionType":"NONE",
                "conditionParams":{

                },
                "createTime":"2022-03-03 00:56:04",
                "updateTime":"2022-03-03 00:56:04",
                "operator":2,
                "operateTime":"2022-03-03 00:56:04"
            }
        ],
        "taskDefinitionList":[
            {
                "id":37,
                "code":4652334706048,
                "name":"test1",
                "version":13,
                "description":"",
                "projectCode":4652156953088,
                "userId":2,
                "taskType":"SHELL",
                "taskParams":{
                    "resourceList":[
                        {
                            "id":6
                        }
                    ],
                    "localParams":[
                        {
                            "prop":"jobId",
                            "direct":"IN",
                            "type":"VARCHAR",
                            "value":"546"
                        }
                    ],
                    "rawScript":"a=1\necho 'aaaaaaaaaaaaaa'\npwd\nwhoami\ncat calc/提交作业.sh\necho ${jobId}\necho $a\necho $JAVA_HOME\necho $SPARK_HOME\n",
                    "dependence":{

                    },
                    "conditionResult":{
                        "successNode":[

                        ],
                        "failedNode":[

                        ]
                    },
                    "waitStartTimeout":{

                    },
                    "switchResult":{

                    }
                },
                "taskParamList":[
                    {
                        "prop":"jobId",
                        "direct":"IN",
                        "type":"VARCHAR",
                        "value":"546"
                    }
                ],
                "taskParamMap":{
                    "jobId":"546"
                },
                "flag":"YES",
                "taskPriority":"MEDIUM",
                "userName":null,
                "projectName":null,
                "workerGroup":"default",
                "environmentCode":4711247356672,
                "failRetryTimes":0,
                "failRetryInterval":1,
                "timeoutFlag":"CLOSE",
                "timeoutNotifyStrategy":"WARN",
                "timeout":0,
                "delayTime":0,
                "resourceIds":"6",
                "createTime":"2022-02-25 16:13:16",
                "updateTime":"2022-03-03 00:24:05",
                "modifyBy":null,
                "operator":2,
                "operateTime":"2022-03-03 00:24:05"
            },
            {
                "id":21,
                "code":4652640021376,
                "name":"calc shell test2",
                "version":11,
                "description":"",
                "projectCode":4652156953088,
                "userId":2,
                "taskType":"SHELL",
                "taskParams":{
                    "resourceList":[

                    ],
                    "localParams":[
                        {
                            "prop":"jobId",
                            "direct":"IN",
                            "type":"VARCHAR",
                            "value":"546"
                        },
                        {
                            "prop":"theDate",
                            "direct":"IN",
                            "type":"VARCHAR",
                            "value":"20220228"
                        }
                    ],
                    "rawScript":"python3 /data/calc_dev/bin/run_job.py -j ${jobId} -d ${theDate}",
                    "dependence":{

                    },
                    "conditionResult":{
                        "successNode":[

                        ],
                        "failedNode":[

                        ]
                    },
                    "waitStartTimeout":{

                    },
                    "switchResult":{

                    }
                },
                "taskParamList":[
                    {
                        "prop":"jobId",
                        "direct":"IN",
                        "type":"VARCHAR",
                        "value":"546"
                    },
                    {
                        "prop":"theDate",
                        "direct":"IN",
                        "type":"VARCHAR",
                        "value":"20220228"
                    }
                ],
                "taskParamMap":{
                    "jobId":"546",
                    "theDate":"20220228"
                },
                "flag":"YES",
                "taskPriority":"MEDIUM",
                "userName":null,
                "projectName":null,
                "workerGroup":"default",
                "environmentCode":-1,
                "failRetryTimes":0,
                "failRetryInterval":1,
                "timeoutFlag":"CLOSE",
                "timeoutNotifyStrategy":"WARN",
                "timeout":0,
                "delayTime":0,
                "resourceIds":"",
                "createTime":"2022-02-25 16:53:16",
                "updateTime":"2022-02-28 12:23:34",
                "modifyBy":null,
                "operator":2,
                "operateTime":"2022-02-28 12:23:34"
            },
            {
                "id":33,
                "code":4708134159488,
                "name":"sql 测试",
                "version":5,
                "description":"",
                "projectCode":4652156953088,
                "userId":1,
                "taskType":"SQL",
                "taskParams":{
                    "type":"MYSQL",
                    "datasource":2,
                    "sql":"SELECT * from test.ods_bugsdaily ",
                    "udfs":"",
                    "sqlType":"0",
                    "sendEmail":false,
                    "displayRows":10,
                    "title":"",
                    "groupId":null,
                    "localParams":[

                    ],
                    "connParams":"",
                    "preStatements":[

                    ],
                    "postStatements":[

                    ],
                    "dependence":{

                    },
                    "conditionResult":{
                        "successNode":[

                        ],
                        "failedNode":[

                        ]
                    },
                    "waitStartTimeout":{

                    },
                    "switchResult":{

                    }
                },
                "taskParamList":[

                ],
                "taskParamMap":{

                },
                "flag":"YES",
                "taskPriority":"MEDIUM",
                "userName":null,
                "projectName":null,
                "workerGroup":"default",
                "environmentCode":4711247356672,
                "failRetryTimes":0,
                "failRetryInterval":1,
                "timeoutFlag":"CLOSE",
                "timeoutNotifyStrategy":"WARN",
                "timeout":0,
                "delayTime":0,
                "resourceIds":"",
                "createTime":"2022-03-02 17:27:15",
                "updateTime":"2022-03-03 00:08:57",
                "modifyBy":null,
                "operator":2,
                "operateTime":"2022-03-03 00:08:57"
            },
            {
                "id":23,
                "code":4708215982720,
                "name":"datax测试",
                "version":1,
                "description":"",
                "projectCode":4652156953088,
                "userId":1,
                "taskType":"DATAX",
                "taskParams":{
                    "customConfig":1,
                    "json":"{\n  \"job\": {\n    \"content\": [\n      {\n        \"reader\": {\n          \"name\": \"streamreader\",\n          \"parameter\": {\n            \"sliceRecordCount\": 10,\n            \"column\": [\n              {\n                \"type\": \"long\",\n                \"value\": \"10\"\n              },\n              {\n                \"type\": \"string\",\n                \"value\": \"hello，你好，世界-DataX\"\n              }\n            ]\n          }\n        },\n        \"writer\": {\n          \"name\": \"streamwriter\",\n          \"parameter\": {\n            \"encoding\": \"UTF-8\",\n            \"print\": true\n          }\n        }\n      }\n    ],\n    \"setting\": {\n      \"speed\": {\n        \"channel\": 10\n       }\n    }\n  }\n}",
                    "localParams":[

                    ],
                    "xms":1,
                    "xmx":1,
                    "dependence":{

                    },
                    "conditionResult":{
                        "successNode":[

                        ],
                        "failedNode":[

                        ]
                    },
                    "waitStartTimeout":{

                    },
                    "switchResult":{

                    }
                },
                "taskParamList":[

                ],
                "taskParamMap":{

                },
                "flag":"YES",
                "taskPriority":"MEDIUM",
                "userName":null,
                "projectName":null,
                "workerGroup":"default",
                "environmentCode":-1,
                "failRetryTimes":0,
                "failRetryInterval":1,
                "timeoutFlag":"CLOSE",
                "timeoutNotifyStrategy":null,
                "timeout":0,
                "delayTime":0,
                "resourceIds":"",
                "createTime":"2022-03-02 17:31:01",
                "updateTime":"2022-03-02 17:31:01",
                "modifyBy":null,
                "operator":1,
                "operateTime":"2022-03-02 17:31:01"
            },
            {
                "id":24,
                "code":4708247313536,
                "name":"datax测试2",
                "version":1,
                "description":"",
                "projectCode":4652156953088,
                "userId":1,
                "taskType":"DATAX",
                "taskParams":{
                    "customConfig":1,
                    "json":"{\n  \"job\": {\n    \"content\": [\n      {\n        \"reader\": {\n          \"name\": \"hdfsreader\",\n          \"parameter\": {\n            \"column\": [\n              \"*\"\n            ],\n            \"defaultFS\": \"hdfs://xx.xx.xxx.xx:4007\",\n            \"fieldDelimiter\": \"\\t\",\n            \"fileType\": \"text\",\n            \"path\": \"/usr/hive/warehouse/test01/aaa/*\"\n          }\n        },\n        \"writer\": {\n          \"name\": \"hdfswriter\",\n          \"parameter\": {\n            \"column\": [\n              {\n                \"name\": \"code\",\n                \"type\": \"string\"\n              },\n              {\n                \"name\": \"message\",\n                \"type\": \"string\"\n              },\n              {\n                \"name\": \"data\",\n                \"type\": \"string\"\n              },\n              {\n                \"name\": \"data_json\",\n                \"type\": \"string\"\n              }\n            ],\n            \"defaultFS\": \"hdfs://xx.xx.xxx.xx:4007\",\n            \"jdbcUrl\": \"jdbc:hive2://xx.xx.xxx.xx:7001/\",\n            \"username\": \"hadoop\",\n            \"password\": \"\",\n            \"database\": \"test01\",\n            \"table\": \"010\",\n            \"tablePath\": \"/usr/hive/warehouse/test01/010\",\n            \"external\": false,\n            \"fieldDelimiter\": \"\\t\",\n            \"fileName\": \"010\",\n            \"fileType\": \"text\",\n            \"path\": \"/usr/hive/warehouse/test01/010\",\n            \"writeMode\": \"append\",\n            \"hiveOverwrite\": true\n          }\n        }\n      }\n    ],\n    \"setting\": {\n      \"speed\": {\n        \"channel\": 1\n      }\n    }\n  }\n}",
                    "localParams":[

                    ],
                    "xms":1,
                    "xmx":1,
                    "dependence":{

                    },
                    "conditionResult":{
                        "successNode":[

                        ],
                        "failedNode":[

                        ]
                    },
                    "waitStartTimeout":{

                    },
                    "switchResult":{

                    }
                },
                "taskParamList":[

                ],
                "taskParamMap":{

                },
                "flag":"YES",
                "taskPriority":"MEDIUM",
                "userName":null,
                "projectName":null,
                "workerGroup":"default",
                "environmentCode":-1,
                "failRetryTimes":0,
                "failRetryInterval":1,
                "timeoutFlag":"CLOSE",
                "timeoutNotifyStrategy":null,
                "timeout":0,
                "delayTime":0,
                "resourceIds":"",
                "createTime":"2022-03-02 17:34:23",
                "updateTime":"2022-03-02 17:34:23",
                "modifyBy":null,
                "operator":1,
                "operateTime":"2022-03-02 17:34:23"
            },
            {
                "id":39,
                "code":4711044355712,
                "name":"spark",
                "version":4,
                "description":"",
                "projectCode":4652156953088,
                "userId":2,
                "taskType":"SPARK",
                "taskParams":{
                    "mainClass":"com.tme.it.spark.Main",
                    "mainJar":{
                        "id":7
                    },
                    "deployMode":"client",
                    "resourceList":[
                        {
                            "id":7
                        }
                    ],
                    "localParams":[

                    ],
                    "driverCores":1,
                    "driverMemory":"512M",
                    "numExecutors":2,
                    "executorMemory":"2G",
                    "executorCores":2,
                    "appName":"dolphin测试",
                    "mainArgs":"H4sIAAAAAAAAAJVTXWvbMBT9K8YQaEdQ5CRrmN8KW1/GGGzdoHTDlaXrRJ1lOdJ1UxPy33clO92WhcGeLN97dO7HOdqnj7YssG8hzVO/rdNpDDTChACCR87nFNzoJygMoCg6p0Nm43SF+WyWcZYtOJuz+XyRrzhfErgVThif5vtUiloyhEY0yEbOjVDWtiMlg2eQTPWU05LRPdSobRMKuA7+DWLGqkDY2Maj0xIJbkTrQHUSGJ2YAWNdz0xJqOw15/wPxBF4BhSrGnDryEPAStfgz3VlxDOTDgSCYkdQxvlvNC+wvwb4LyxrwTXDwOOdwzR1tsNQ8n6fllBZN2Yz4hMVght+5+nh+zRF4X9ETSKAvp2rKf+oSpmbnqT/JebVkmVvVvliwa9mwQJE1/nI5qzFKLD3O+sURd5v+c3dl89yGVyCoqxDD1b5ouzWXgld9yEOpv2qYXeaKtAEJ0hbdyas4576DCYkK0INEpNX07fXt++Km4+fPlzfXpDgutKgpt86mmo1uZuYiRrOl0mrRJ9UzprktPpovDhkQVopGoXqoCPV1v0xw8YMrTXujDZ0dGzw/s5pPL0XNRsezdC1bmhNmNgncBGfxIUkYYfFVrAH4n1IXjS9CC1fJsdRzzQf93M4HH4C/3SldKYDAAA= /data/calc/conf/dev_conf \" \" /data/calc_dev/logs/run_logs/10001/20220302/SPARK_546_202203022344.log ",
                    "others":"",
                    "programType":"JAVA",
                    "sparkVersion":"SPARK1",
                    "dependence":{

                    },
                    "conditionResult":{
                        "successNode":[

                        ],
                        "failedNode":[

                        ]
                    },
                    "waitStartTimeout":{

                    },
                    "switchResult":{

                    }
                },
                "taskParamList":[

                ],
                "taskParamMap":{

                },
                "flag":"YES",
                "taskPriority":"MEDIUM",
                "userName":null,
                "projectName":null,
                "workerGroup":"default",
                "environmentCode":4711247356672,
                "failRetryTimes":0,
                "failRetryInterval":1,
                "timeoutFlag":"CLOSE",
                "timeoutNotifyStrategy":"WARN",
                "timeout":0,
                "delayTime":0,
                "resourceIds":"7",
                "createTime":"2022-03-02 23:45:59",
                "updateTime":"2022-03-03 00:56:04",
                "modifyBy":null,
                "operator":2,
                "operateTime":"2022-03-03 00:56:04"
            }
        ],
        "schedule":null
    }
]
```





dolphin 系统时间参数使用

```

变量	含义
${system.biz.date}	日常调度实例定时的定时时间前一天，格式为 yyyyMMdd，补数据时，该日期 +1
${system.biz.curdate}	日常调度实例定时的定时时间，格式为 yyyyMMdd，补数据时，该日期 +1
${system.datetime}	日常调度实例定时的定时时间，格式为 yyyyMMddHHmmss，补数据时，该日期 +1
参数里是引用系统参数，也可以是指定常量。也可以使用以下格式
* 后 N 年：$[add_months(yyyyMMdd,12*N)] 
* 前 N 年：$[add_months(yyyyMMdd,-12*N)] 
* 后 N 月：$[add_months(yyyyMMdd,N)] 
* 前 N 月：$[add_months(yyyyMMdd,-N)] 
* 后 N 周：$[yyyyMMdd+7*N] 
* 前 N 周：$[yyyyMMdd-7*N] 
* 后 N 天：$[yyyyMMdd+N] 
* 前 N 天：$[yyyyMMdd-N] 
* 后 N 小时：$[HHmmss+N/24] 
* 前 N 小时：$[HHmmss-N/24] 
* 后 N 分钟：$[HHmmss+N/24/60] 
* 前 N 分钟：$[HHmmss-N/24/60]
```



#### 四、dolphin和airflow对比

|                        | **DolphinScheduler**                                         | **Airflow**                          |
| ---------------------- | ------------------------------------------------------------ | ------------------------------------ |
| 稳定性                 | 去中心化的多Master和多worker                                 | 单一调度程序                         |
| 易用性                 | 流程dag自定义可视化操作                                      | 通过python代码来绘制DAG              |
| 快速部署               | 一键部署                                                     | 集群化部署复杂                       |
| 是否能暂停和恢复       | 支持暂停，恢复操作                                           | 否<br/> 只能先将工作流杀死再重新运行 |
| 是否支持多租户         | 支持，可对不同的dag作业设置不同的租户执行                    | 否，dag执行用户固定启动用户root      |
| 是否支持自定义任务类型 | 是                                                           | 是                                   |
| 是否支持集群扩展       | 是<br/> 调度器使用分布式调度，整体的调度能力会随便集群的规模线性增长，Master和Worker支持动态上下线 | 是，但是复杂                         |

现有airflow基于docker部署，作业调用中台sdk需要修改脚本，与中台脚本功能一致，维护麻烦

airflow dag执行目前固定执行用户root，对于切换租户执行作业无法支持，dolphin可以指定租户执行

dolphin用户资源隔离，不同用户只能访问对应的dag和资源

dolphin可以指定全局参数和局部参数，可以指定不同的环境变量

dolphin自带文件管理，可以存储通用配置文件，脚本,jar包，udf函数

airflow dag迁移至dolphinscheduler需要做的工作：需手动绘制dag流程，定义参数

dolphin支持dag导出为json，可以实现集群间dag作业迁移

dolphin可以直接访问数据源，可新建作业直接sql查询数据库，实现指标监控
