## clickhouse表引擎
clickhouse表引擎调研     
|表引擎|是否支持主键|mutation是否支持|optimize是否支持|     
|---|---|---|---|      
|MergeTree|Y|Y|N|
|ReplacingMergeTree|Y|Y|N|      
|TinyLog|N|N|N|      


* ALTER 仅支持 *MergeTree ，Merge以及Distributed等引擎表      
* mutation语法
ALTER table test delete where id = 111;
ALTER table test update name = 'aa',age = 18 where id IN (SELECT id FROM test where eventTime = '2020-02-22');

ALTER table test delete 时必须有where条件否则语法错误，如果确实要删除表中全部数据则可以drop table create table方式实现

* 触发数据合并语法
optimize table aa;

### Clickhouse条件覆盖数据调研

#### Clickhouse写入数据条件覆盖方式调研结论
1、spark默认jdbc write使用SaveMode.ErrorIfExists方式，即如果数据已存在则直接异常退出，不是我们想要的结果，因此我们不能使用默认配置，需单独配置      
2、spark jdbc write使用SaveMode.Overwrite方式可将重复的数据或表进行overwite覆盖，但是向clickhouse 中overwrite时会先将表删除重建表插入数据（这样只能进行全量覆盖，无法实现条件覆盖，同时重建的表中数据不允许存在空值），无法满足我们条件覆盖需求，这样看来想要一步到位实现根据条件覆盖更新数据是做不到的，我们可以将如何覆盖和如何解决数据重复问题拆成两个来分析解决；      
3、spark jdbc write使用SaveMode.Append方式可以实现数据追加功能，即如果数据已存在则继续追加写入，这样可能会造成数据重复（如果在追加前将已存在数据删除再写入即可实现条件覆盖）；我们只需要解决数据重复的问题即可；        

#### 数据重复问题调研
1、ReplacingMergeTree引擎可设置Order by 列，数据写入时新版本的数据会覆盖旧版本数据，但这里说的覆盖不会在写入时主动覆盖；需要依赖引擎自身定时合并数据或者手工执行optimize table table_name触发数据合并；而且只适用于ReplacingMergeTree引擎，其他合并树引擎和日志引擎不支持；故使用该方案解决数据重复不合适；       
2、Clickhouse可手工执行命令Alter table table_name delete where *** 可触发引擎数据裂变（按条件删除数据，合并剩余数据），与SaveMode.Append方式结合使用可实现按条件覆盖更新数据功能；但仅适用于合并树引擎，日志引擎不支持；       

#### 结论
通过上面两个调研分析我们可以看出我们选择执行命令 Alter table table_name delete where *** 结合SaveMode.Append模式实现条件覆盖功能；       
Alter table table_name delete where *** 我们需要通过jdbc连接clickhouse方式触发数据裂变      

clickhouse write中可以通过添加write_mode="overwrite"  、 mutation_condition="pday=20210603" 方式指定数据覆盖条件；样例作业如下：      
```json
{
    "job_type":"config",
    "job_name":"test001",
    "params":{
        "key1":"value1",
        "key2":"value2"
    },
    "hive_meta_uri":"thrift://*.*.*.*:7004",
    "routes":[
        {
            "before":"1001",
            "after":"1002"
        }
    ],
    "tasks":{
        "1001":{
            "sql":"select * from test01",
            "tempView":"tmp1",
            "name":"hive.sql",
            "strategy":"hive.sql"
        },
        "1002":{
            "url":"jdbc:clickhouse://*.*.*.*:8123/HDP_QA_DWS",
            "table":"test01",
            "user":"default",
            "password":"tme!it@admin",
            "write_mode":"overwrite",
            "num_partitions":1,
            "is_transaction":"NONE",
            "batch_size":"3000",
            "tempView":"tmp1",
            "name":"clickhouse_writer",
            "strategy":"clickhouse.writer",
            "mutation_condition":"pday=20210603"
        }
    }
}
```
该作业目的是为了从hive中查询数据，按照指定日期条件覆盖写入到clickhouse表中

### clickhouse replicated分布式表数据写入后时而可以查到时而查不到
使用optimize table 
alter table ,insert
更新优化参数
三种方式都结果都没有根本解决该问题；

* 后通过以下语句查询作业执行计划及报错信息；
SELECT hostname() as hostname,exception_code,exception,query,query_duration_ms FROM clusterAllReplicas('default_cluster',system.query_log) WHERE lower(query) like '%truncate %' and exception_code != 0;

* 最终定位到表定义的partition字段没有设置默认值，而导入时该列内容为空所以数据会被覆盖掉，看不到追加的数据；
* 通过优化partition列和规范partition列值，该问题解决