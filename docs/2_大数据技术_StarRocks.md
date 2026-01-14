

### starrocks
#### 配置优化建议
1. buckets设置建议为 be数量 * cpu核数 / 2 , 为默认值
2. buckets 合并信息存储在表 information_schema.partitions_meta
3. buckets 数据存量建议 bucket存储量合理范围 例如1GB~10GB

为什么计算快，与clickhouse的区别
架构原理： pipeline、查询运行原理、数据存储结构与原理、cache + spill运行机制、compaction机制、低基数字典、资源隔离、权限、物化视图运行原理、streamLoad 过程、存算分离运行机制、集群灾备
应用场景
问题分析定位思路，集群级的和查询提效级的
常见问题与解决方案
遇到哪些生产级问题故障
优劣势、与其他产品比较
* 调优经验
* 问题处理经验


#### 内部表cache
https://docs.starrocks.io/zh/docs/3.1/using_starrocks/caching/block_cache/#%E6%9F%A5%E7%9C%8B-data-cache-%E7%8A%B6%E6%80%81
1. **`starrocks/storage/starlet_cache`**:
- **存储内容**: 该目录用于存算分离内表的缓存数据。启用存算分离后，数据被缓存以提高查询性能，减少数据读取延迟。
- **相关参数**:
    - `storage_root_path`: 指定存储路径。
    - `starlet_use_star_cache`: 启用或禁用 starlet 缓存功能，内表云存储专用，true为 Data Cache ,false为 File Cache 。
    - `starlet_star_cache_disk_size_percent`:  starlet_use_star_cache = true为 Data Cache时，配置 starlet 缓存占用磁盘空间的百分比。单位：数字。
    - starlet_cache_evict_low_water、starlet_cache_evict_high_water： starlet_use_star_cache = false为 File Cache，磁盘剩余空间百分比上下限，剩余空间小于下限则触发缓存淘汰；
      参考： https://docs.starrocks.io/zh/docs/3.1/administration/management/BE_configuration/

2. **`starrocks/data_cache`**:
    - **存储内容**: 该目录用于数据湖 catalog 的缓存数据，主要用于加速数据读取和查询性能。
    - **相关参数**:
        - `block_cache_disk_path`: 指定数据块缓存的路径。
        - `block_cache_disk_size`: 配置数据块缓存的大小。单位：字节。

3. **`starrocks/cn`**:
    -   怀疑是日志。
    - **确认文件**: 可以使用以下命令查看 `cn` 目录下的具体文件和文件夹，以确认存储内容：
      ls -lhR /path/to/starrocks/cn

cache
https://docs.starrocks.io/zh/docs/data_source/data_cache/
spill to disk
https://docs.starrocks.io/zh/docs/administration/management/resource_management/spill_to_disk/

spill触发落盘：sql中包含聚合、关联、排序时spill到磁盘，内存 80% -》本地磁盘
spill触发磁盘回收：执行完后自动回收，任务并行多，写的多；BE节点故障挂掉可能会留下脏数据；
cos缓存触发缓存：访问cos数据都会缓存磁盘；访问远程存储、外部catalog都会参数cache
cos缓存触发回收：所占比例到80%，触发根据LRU策略清理历史缓存，即达到一定阈值后将冷数据逻辑标记为删除，新数据载入缓存时覆盖标记删除的位置；
磁盘告警触发条件：使用率90%
spill_mode=force会比内存慢2、3倍
starlet_star_cache_disk_size_percent=20
block_cache_disk_size=107374182400

1. **`starrocks/storage/starlet_cache`
   重启不会清空，会保留下来。

2. **`starrocks/data_cache`**:
   重启会清空吧。

#### 数据湖外部表datacache
enable_populate_block_cache=true  开启数据湖外部表，datacache
https://docs.starrocks.io/zh/docs/3.1/data_source/data_cache/


#### BE内存限额
SR BE节点物理内存256GB，但Starrocks内部计算可用内存有一套公式，计算完后为211GB，
内存到达VM的85%左右就会出现该问题；
计算公式： VM mem 247GB * mem_limit 0.95 * BE内部可用内存参数0.9  = 211GB

#### starrocks中权限分布查询获取
sys库中  grants_to_roles、grants_to_users两张表有记录权限信息，但如果集群中有external catalog表太多时，查询会报错，可以将下面配置设置为false 避免
《StarRocks权限系统介绍与实践》
enable_show_external_catalog_privilege  很多用户其实是因为catalog外表太多了，他赋权ALL的话，这里一展开就多了。 这个参数设置为false，就只显示SR内表，就不会有那个问题了。

#### sql 优化
-- 开启 profile
set enable_profile = true;
-- 根据执行 sql 记录分析执行计划
show profilelist [limit 5];
analyze profile from '2e0a8cf3-f7fb-11ef-be60-5254001ebd02';
-- 模拟分析 sql profile 执行计划；select 的结果会抛弃，insert 不会实际写入数据，只做模拟
explain analyze sql statement;


#### 查看BE配置
htpp://be_ip:8040/mem_tracker

#### 打印jvm堆栈和内存信息
jstack $fepid > jstack.log
jmap -histo $fepid > jmap_no_live.txt
jmap -histo:live $fepid > jmap_live.txt


#### 分析tablet容量和当前fe内存适配，判断是否要扩容
SHOW PROC '/statistic'
tablet个数和jvm关系：
100w以下16g
100w-200w 32g
200w-500w 64g
500w-1000w 128g
分析当前tablet存储是否合理，和优化操作步骤
https://starrocks.feishu.cn/docx/Onp3d6WUvodosmxEUXlcw3jPnzf

#### 数据分布
https://zhuanlan.zhihu.com/p/687416857
分区
https://docs.starrocks.io/zh/docs/best_practices/partitioning/
分桶
https://docs.starrocks.io/zh/docs/best_practices/bucketing/
查询调优
https://docs.starrocks.io/zh/docs/category/query-tuning/
分区：为了提高数据检索速度，表根据字段按模块分割存储的单元；官方解释：分区允许 StarRocks 在查询时通过分区裁剪跳过整个数据块，并启用仅元数据的生命周期操作，如删除旧数据或特定租户的数据。
分桶：为进一步提高检索速度，分区内在度根据字段分布到多个存储文件； 官方解释：分桶则有助于将数据分布在多个 tablet 上，以并行化查询执行和均衡负载，特别是在与哈希函数结合使用时。
tablet: 分桶后存储的物理文件

#### 执行计划
https://mp.weixin.qq.com/s?__biz=MzI1MTYxOTkxNQ==&mid=2247485848&idx=1&sn=90e47d7a46eb120701d28b5acfbcc401&chksm=e9f176bcde86ffaaa0c4a3b686eea5b053ff286164cb9a27e85daf4e42bec08985f5a41975c3&scene=21#wechat_redirect
https://baijiahao.baidu.com/s?id=1757699161745238199&wfr=spider&for=pc
https://zhuanlan.zhihu.com/p/28596449822
https://blog.csdn.net/feidodoxcx/article/details/127649514
https://docs.starrocks.io/zh/docs/introduction/Features/




使用 stargo 部署 starrocks 集群命令
cd /data/app/stargo-v2.3; ./stargo cluster deploy sr v3.1.11 deploy.yaml
重启集群命令
cd /data/app/stargo-v2.3; ./stargo cluster restart sr

集群扩容，将扩容的节点信息编写在 scale-out.yaml 中
./stargo cluster scale-out sr ./scala-out.yaml
集群缩容  ./stargo cluster display sr 查看 nodeid
./stargo cluster scale-in sr --node ${node_id}

参考其他 fe 节点启动 fe
./fe/bin/start_fe.sh --helper <leader_ip>:<leader_edit_log_port> --daemon

部署 udf到 httpd



RD确认manager迁移默认用了JDK8，如果3.1.10版本后manager升级就默认使用JDK11，这块也会把manager迁移默认用JDK11。
集群修改JDK11的方法：
需要修改 agent/supervisor/conf.d/{fe,be,apache_hdfs_broker}-xxx.conf 中的environment为jdk-11，然后agentctl.sh 重启进程就行了。
这个就是改回JDK11的办法，如果现在使用JDK8，FE JVM用的是JAVA_OPTS，如果是JDK11，FE JVM用的是 JAVA_OPTS_FOR_JDK_9
sed -i 's/jdk-8/jdk-11/g' /data/app/starrocks-manager-console/agent/supervisor/conf.d/*.conf

RD看了是触发JDK11的bug，可以修改agent/jdk-11/conf/security/java.security 文件来禁止使用tlsv3，jdk.tls.disabledAlgorithms 这个配置末尾加个 , TLSv1.3
vi /data/app/starrocks-manager-console/agent/jdk-11/conf/security/java.security
jdk.tls.disabledAlgorithms 后追加 TLSv1.3

/data/app/starrocks-manager-console/agentctl.sh update
/data/app/starrocks-manager-console/agentctl.sh restart all

## 查看 慢 sql
tail -n 500 fe.audit.log | awk -F '|' '{print $12}' |awk -F '=' '{print $2}' |sort -r -n |uniq|head

tail -n 500 log/fe.audit.log | awk -F '|' '{print $12}' |awk -F '=' '{print $2}' |sort -r -n |uniq|head

## be 参数查看
获取 be 节点配置
curl -XGET -s http://xx.xx.xxx.xx:8040/metrics | grep "^starrocks_be_.*_mem_bytes\|^starrocks_be_tcmalloc_bytes_in_use"
获取 be 节点内存使用情况
curl -XGET -s http://xx.xx.xxx.xx:8040/mem_tracker

## 查看后台在执行的pipeline sql
curl http://127.0.0.1:8040/api/pipeline_blocking_drivers/stat|grep '"query_id"'

## sr数据合并是按 partition 合并的，查询 patition信息和数据 compaction 信息
select * from information_schema.partitions_meta order by Max_CS desc;
select * from information_schema.be_cloud_native_compactions;

## 查询配置信息
SELECT * FROM information_schema.be_configs WHERE NAME LIKE "%starlet_use_star_cache%";


## Starrocks manager 相关命令
建用户
useradd starrocks
passwd starrocks
chage -m 0 -M 99999 -I -1 -E -1 starrocks
查看密码状态
chage -l starrocks
su - starrocks
# ssh-keygen
## 将中控机上的 ssh 公钥上传到其他节点上，如果需要密码就输入密码
# ssh-copy-id starrocks@{fe ip}



manager 安装命令
解压tar包到 /data/app/
/data/app/MirrorShip-EE-3.1.14/bin/install.sh -d /data/app/starrocks-manager

supervisord 重启命令
kill -9 ${supervisord_pid}
python3 /data/app/starrocks-manager/center/supervisor/bin/supervisord -c /data/app/starrocks-manager/center/supervisor/supervisor.conf
停止center_service 和 supervisord 服务
./centerctl.sh stop all
./centerctl.sh shutdown
启动center_service 和 supervisord 服务
./centerctl.sh daemon


停止agent_service 和 supervisord 服务
./agentctl.sh stop all
./agentctl.sh shutdown
启动agent_service 和 supervisord 服务
./agentctl.sh daemon


manager centerctl命令用于管理 manager 本身
启动centerctl
sh centerctl.sh daemon
查看组件
sh centerctl.sh status
停止组件
sh centerctl.sh stop all
sh centerctl.sh stop center-service或web
启动组件
sh centerctl.sh start all
sh centerctl.sh start center-service或web
重启组件
sh centerctl.sh restart all
sh centerctl.sh restart center-service或web
关闭centerctl
sh centerctl.sh shutdown

通过manager 命令行操作 agentctl命令用于管理企业版中的 进程、服务
启动agentctl
sh agentctl.sh daemon
查看组件
sh agentctl.sh status
停止组件
sh agentctl.sh stop all
sh agentctl.sh stop fe-xxx或be-xxx
启动组件
sh agentctl.sh start all
sh agentctl.sh start fe-xxx或be-xxx
重启组件
sh agentctl.sh restart all
sh agentctl.sh restart fe-xxx或be-xxx
关闭agentctl
sh agentctl.sh shutdown


直接后台启动 cn
/data/starrocks/cn/bin/start_cn.sh --daemon [--logconsole]

查看 hive_catalog 启动 HADOOP_USERNAME
cat /proc/<pid>/environ | tr '\0' '\n' | grep HADOOP_USERNAME
pid是FE，CN的进程号，试试这个

查看当前执行 sql
SHOW PROC '/current_queries' ;

## 强制 FE 切主  sql client 可以访问但不能执行 sql 情况下有效；
java -jar fe/lib/je-7.3.7.jar DbGroupAdmin -helperHosts {fe_master_ip:edit_log_port} -groupName PALO_JOURNAL_GROUP -transferMaster -force {node_name} 5000 其中节点信息为要切的节点的信息，node_name 通过show frontends 查看，执行时需删除{}。
je文件根据版本来确定  ls *-je-*.jar
2.5版本以前(包括2.5) je-7.3.7.jar
3.0版本以后(包括3.0) starrocks-bdb-je-18.3.16.jar
命令样例：
java -jar /data/starrocks/fe/lib/starrocks-bdb-je-18.3.16.jar DbGroupAdmin -helperHosts xx.xx.xxx.xx:9010 -groupName PALO_JOURNAL_GROUP -transferMaster -force xx.xx.xxx.xx_9010_1724921851812 5000

java -jar /data/starrocks/fe/lib/starrocks-bdb-je-18.3.16.jar DbGroupAdmin -helperHosts xx.xx.xxx.xx:9010 -groupName PALO_JOURNAL_GROUP -transferMaster -force xx.xx.xxx.xx_9010_1724938503351 5000

## 让集群生成 image
链接集群执行 sql
ALTER SYSTEM CREATE IMAGE;

## 查看 bukelt compact 情况
```sql
select * from information_schema.partitions_meta order by Max_CS desc;
select * from information_schema.be_cloud_native_compactions;
show proc '/compactions';

enable_legacy_compatibility_for_replication = true
```

storage_page_cache_limit = 10%
block_cache_mem_size = 68719476736

cn
storage_page_cache_limit = 10%
fe
spill_mem_limit_threshold

storage_root_path = /data/starrocks/cn/storage 存储数据的目录以及存储介质类型
block_cache_disk_path = /data/starrocks/cn/data_cache  存储数据的目录以及存储介质类型
block_cache_disk_size   单个磁盘缓存数据量的上限，单位：字节
spill_local_storage_dir = /data/starrocks/cn/spill_local_storage_dir 中间结果落盘路径
block_cache_enable = true  是否启用 Data Cache
block_cache_mem_size = 68719476736
brpc_socket_max_unwritten_bytes = 2147483648
starlet_star_cache_disk_size_percent=20


## 使用 fe meta目录恢复集群时，如遇异常通过下面配置强制节点为 leader 启动
异常信息： wait globalStateMgr to be ready. FE type: INIT. is ready: false
bdbje_reset_election_group = true
参考： https://docs.starrocks.io/zh/docs/administration/Meta_recovery/#7-%E6%9C%80%E7%BB%88%E5%BA%94%E6%80%A5%E6%96%B9%E6%A1%88

## starrocks 存储相关配置
/data/starrocks/cn/data_cache
下面两个存储目前较大，但目前未检查到使用   lsof
/data/starrocks/data_cache/
/data/starrocks/storage/starlet_cache/

find /data/starrocks/cn/data_cache -type f -mtime -1
find /data/starrocks/data_cache -type f -mtime -1
find /data/starrocks/storage -type f -mtime -1


rm -rf /data/starrocks/cn/data_cache/*
rm -rf /data/starrocks/cn/spill_local_storage_dir/*
rm -rf /data/starrocks/cn/storage/meta/*
rm -rf /data/starrocks/cn/storage/persistent/*
rm -rf /data/starrocks/cn/storage/starlet_cache/*

### 内部表cache
https://docs.starrocks.io/zh/docs/3.1/using_starrocks/caching/block_cache/#%E6%9F%A5%E7%9C%8B-data-cache-%E7%8A%B6%E6%80%81
1. **`starrocks/storage/starlet_cache`**:
- **存储内容**: 该目录用于存算分离内表的缓存数据。启用存算分离后，数据被缓存以提高查询性能，减少数据读取延迟。
- **相关参数**:
    - `storage_root_path`: 指定存储路径。
    - `starlet_use_star_cache`: 启用或禁用 starlet 缓存功能，内表云存储专用，true为 Data Cache ,false为 File Cache 。
    - `starlet_star_cache_disk_size_percent`:  starlet_use_star_cache = true为 Data Cache时，配置 starlet 缓存占用磁盘空间的百分比。单位：数字。
    - starlet_cache_evict_low_water、starlet_cache_evict_high_water： starlet_use_star_cache = false为 File Cache，磁盘剩余空间百分比上下限，剩余空间小于下限则触发缓存淘汰；
      参考： https://docs.starrocks.io/zh/docs/3.1/administration/management/BE_configuration/

2. **`starrocks/data_cache`**:
    - **存储内容**: 该目录用于数据湖 catalog 的缓存数据，主要用于加速数据读取和查询性能。
    - **相关参数**:
        - `block_cache_disk_path`: 指定数据块缓存的路径。
        - `block_cache_disk_size`: 配置数据块缓存的大小。单位：字节。

3. **`starrocks/cn`**:
    -   怀疑是日志。
    - **确认文件**: 可以使用以下命令查看 `cn` 目录下的具体文件和文件夹，以确认存储内容：
      ls -lhR /path/to/starrocks/cn

cache
https://docs.starrocks.io/zh/docs/data_source/data_cache/
spill to disk
https://docs.starrocks.io/zh/docs/administration/management/resource_management/spill_to_disk/

spill触发落盘：sql中包含聚合、关联、排序时spill到磁盘，内存 80% -》本地磁盘
spill触发磁盘回收：执行完后自动回收，任务并行多，写的多；BE节点故障挂掉可能会留下脏数据；
cos缓存触发缓存：访问cos数据都会缓存磁盘；访问远程存储、外部catalog都会参数cache
cos缓存触发回收：所占比例到80%，触发根据LRU策略清理历史缓存，即达到一定阈值后将冷数据逻辑标记为删除，新数据载入缓存时覆盖标记删除的位置；
磁盘告警触发条件：使用率90%
spill_mode=force会比内存慢2、3倍
starlet_star_cache_disk_size_percent=20
block_cache_disk_size=107374182400

1. **`starrocks/storage/starlet_cache`
   重启不会清空，会保留下来。

2. **`starrocks/data_cache`**:
   重启会清空吧。

### 数据湖外部表datacache
enable_populate_block_cache=true  开启数据湖外部表，datacache
https://docs.starrocks.io/zh/docs/3.1/data_source/data_cache/


## BE内存限额
SR BE节点物理内存256GB，但Starrocks内部计算可用内存有一套公式，计算完后为211GB，
内存到达VM的85%左右就会出现该问题；
计算公式： VM mem 247GB * mem_limit 0.95 * BE内部可用内存参数0.9  = 211GB

## starrocks中权限分布查询获取
sys库中  grants_to_roles、grants_to_users两张表有记录权限信息，但如果集群中有external catalog表太多时，查询会报错，可以将下面配置设置为false 避免
《StarRocks权限系统介绍与实践》
enable_show_external_catalog_privilege  很多用户其实是因为catalog外表太多了，他赋权ALL的话，这里一展开就多了。 这个参数设置为false，就只显示SR内表，就不会有那个问题了。

## sql 优化
-- 开启 profile
set enable_profile = true;
-- 根据执行 sql 记录分析执行计划
show profilelist [limit 5];
analyze profile from '2e0a8cf3-f7fb-11ef-be60-5254001ebd02';
-- 模拟分析 sql profile 执行计划；select 的结果会抛弃，insert 不会实际写入数据，只做模拟
explain analyze sql statement;
-- 获取 sql 执行 dump 信息，比 profile 内容更加详细；
wget --user=root --password=dOGD0FV1aL --post-file no_spill.txt "http://xx.xx.xxx.xx:8030/api/query_dump?db=SR_FIN_DWS&mock=false" -O no_spill_dump.txt

-- sql 前加 TRACE LOGS OPTIMIZER 后运行 sql，会将报错详细输出到 fe.log 中
TRACE LOGS OPTIMIZER


## 查看BE配置
htpp://be_ip:8040/mem_tracker

## 打印jvm堆栈和内存信息
jstack $fepid > jstack.log
jmap -histo $fepid > jmap_no_live.txt
jmap -histo:live $fepid > jmap_live.txt


## 分析tablet容量和当前fe内存适配，判断是否要扩容
SHOW PROC '/statistic'
tablet个数和jvm关系：
100w以下16g
100w-200w 32g
200w-500w 64g
500w-1000w 128g
分析当前tablet存储是否合理，和优化操作步骤
https://starrocks.feishu.cn/docx/Onp3d6WUvodosmxEUXlcw3jPnzf

### 数据分布
https://zhuanlan.zhihu.com/p/687416857
分区
https://docs.starrocks.io/zh/docs/best_practices/partitioning/
分桶
https://docs.starrocks.io/zh/docs/best_practices/bucketing/
查询调优
https://docs.starrocks.io/zh/docs/category/query-tuning/
分区：为了提高数据检索速度，表根据字段按模块分割存储的单元；官方解释：分区允许 StarRocks 在查询时通过分区裁剪跳过整个数据块，并启用仅元数据的生命周期操作，如删除旧数据或特定租户的数据。
分桶：为进一步提高检索速度，分区内在度根据字段分布到多个存储文件； 官方解释：分桶则有助于将数据分布在多个 tablet 上，以并行化查询执行和均衡负载，特别是在与哈希函数结合使用时。
tablet: 分桶后存储的物理文件

### 执行计划
https://mp.weixin.qq.com/s?__biz=MzI1MTYxOTkxNQ==&mid=2247485848&idx=1&sn=90e47d7a46eb120701d28b5acfbcc401&chksm=e9f176bcde86ffaaa0c4a3b686eea5b053ff286164cb9a27e85daf4e42bec08985f5a41975c3&scene=21#wechat_redirect
https://baijiahao.baidu.com/s?id=1757699161745238199&wfr=spider&for=pc
https://zhuanlan.zhihu.com/p/28596449822
https://blog.csdn.net/feidodoxcx/article/details/127649514
https://docs.starrocks.io/zh/docs/introduction/Features/



## SR管理命令

sr 自带工具清理
当导入或者后台 Compaction 任务由于种种原因失败时，在对象存储中可能会产生垃圾文件，老版本中未能清理这些文件也可能会产生垃圾。此时，可利用我们提供的手动清理脚本工具来协助清理这些垃圾文件
nohup ./bin/meta_tool.sh --operation=lake_datafile_gc --root_path="s3://starrocks-test-1253428821/default/2accd6c3-7bbb-4c6d-a7cd-ac90e2b96c0a/db5342690/16670075" --expired_sec=86400 --conf_file=/data/starrocks/cn/conf/cn.conf --audit_file=/data/starrocks/cn/audit.txt --do_delete=false &

查看当前各库统计信息
show proc '/statistic'
同网页 http://${feIP}:8030/system?path=//statistic
```text
DbId    |DbName              |TableNum|PartitionNum|IndexNum|TabletNum|ReplicaNum|UnhealthyTabletNum|InconsistentTabletNum|CloningTabletNum|ErrorStateTabletNum|
--------+--------------------+--------+------------+--------+---------+----------+------------------+---------------------+----------------+-------------------+
22114   |SR_FIN_ADS          |180     |706         |706     |6088     |0         |0                 |0                    |0               |0                  |
22115   |SR_FIN_APP          |9       |9           |9       |91       |0         |0                 |0                    |0               |0                  |
22116   |SR_FIN_DIM          |71      |71          |71      |537      |0         |0                 |0                    |0               |0                  |
22117   |SR_FIN_DWD          |56      |60          |60      |482      |0         |0                 |0                    |0               |0                  |
22118   |SR_FIN_DWS          |58      |1024        |1024    |8771     |0         |0                 |0                    |0               |0                  |
22120   |SR_FIN_NDS          |300     |300         |300     |4730     |0         |0                 |0                    |0               |0                  |
22121   |SR_FIN_ODS          |75      |75          |75      |718      |0         |0                 |0                    |0               |0                  |
```

查看当前库中有哪些表，及表的使用和对应cos信息
show proc '/dbs/SR_FIN_TMP'
同网页  http://${feIP}:8030/system?path=//dbs/11637489
```text
TableId |TableName                                             |IndexNum|PartitionColumnName|PartitionNum|State |Type                          |LastConsistencyCheckTime|ReplicaCount|PartitionType|StoragePath                                                                             |
--------+------------------------------------------------------+--------+-------------------+------------+------+------------------------------+------------------------+------------+-------------+----------------------------------------------------------------------------------------+
31733   |tmp_bu_step5_complex_index                            |1       |                   |1           |NORMAL|CLOUD_NATIVE                  |                        |6           |UNPARTITIONED|s3://starrocks-test-1253428821/fin/2accd6c3-7bbb-4c6d-a7cd-ac90e2b96c0a/db22119/31733   |
31751   |tmp_fin_aging_writeoff2                               |1       |                   |1           |NORMAL|CLOUD_NATIVE                  |                        |9           |UNPARTITIONED|s3://starrocks-test-1253428821/fin/2accd6c3-7bbb-4c6d-a7cd-ac90e2b96c0a/db22119/31751   |
31772   |tmp_fin_aging_writeoff1                               |1       |                   |1           |NORMAL|CLOUD_NATIVE                  |                        |9           |UNPARTITIONED|s3://starrocks-test-1253428821/fin/2accd6c3-7bbb-4c6d-a7cd-ac90e2b96c0a/db22119/31772   |
31796   |remove_data_check                                     |1       |check_range        |13          |NORMAL|CLOUD_NATIVE                  |                        |52          |RANGE        |s3://starrocks-test-1253428821/fin/2accd6c3-7bbb-4c6d-a7cd-ac90e2b96c0a/db22119/31796   |
31860   |tmp_bu_step2_base_data                                |1       |                   |1           |NORMAL|CLOUD_NATIVE                  |                        |6           |UNPARTITIONED|s3://starrocks-test-1253428821/fin/2accd6c3-7bbb-4c6d-a7cd-ac90e2b96c0a/db22119/31860   |
```

查看表数据量和大小
use SR_FIN_TMP
show data from tmp_fin_fsg_profit_details_1

-- 查看tablet 信息
select * from information_schema.tables
select * from information_schema.tables_config
show tablet 11020063


查看表的分桶数和表大小
show  data from sr_hr_cryptic_dwd.dwd_hr_cost_empl_detail_1dfsp;

```text
TableName                    |IndexName                    |Size     |ReplicaCount|RowCount|
-----------------------------+-----------------------------+---------+------------+--------+
dwd_hr_cost_empl_detail_1dfsp|dwd_hr_cost_empl_detail_1dfsp|19.326 MB|39168       |3987344 |
                             |Total                        |19.326 MB|39168       |        |
```

查看表的每个分区分桶数
show partitions from  sr_hr_cryptic_dwd.dwd_hr_cost_empl_detail_1dfsp;
```text

PartitionId|PartitionName|CompactVersion|VisibleVersion|NextVersion|State |PartitionKey|Range                                                               |DistributionKey|Buckets|DataSize|RowCount|EnableDataCache|AsyncWrite|AvgCS|P50CS|MaxCS|
-----------+-------------+--------------+--------------+-----------+------+------------+--------------------------------------------------------------------+---------------+-------+--------+--------+---------------+----------+-----+-----+-----+
8213836    |p10201601    |0             |1             |2          |NORMAL|cost_source |[types: [INT]; keys: [10201601]; ..types: [INT]; keys: [10201602]; )|cost_source    |24     |0B      |0       |true           |false     |0.00 |0.00 |0.00 |
8213837    |p10201602    |0             |1             |2          |NORMAL|cost_source |[types: [INT]; keys: [10201602]; ..types: [INT]; keys: [10201603]; )|cost_source    |24     |0B      |0       |true           |false     |0.00 |0.00 |0.00 |
8213838    |p10201603    |0             |1             |2          |NORMAL|cost_source |[types: [INT]; keys: [10201603]; ..types: [INT]; keys: [10201604]; )|cost_source    |24     |0B      |0       |true           |false     |0.00 |0.00 |0.00 |
8213839    |p10201604    |0             |1             |2          |NORMAL|cost_source |[types: [INT]; keys: [10201604]; ..types: [INT]; keys: [10201605]; )|cost_source    |24     |0B      |0       |true           |false     |0.00 |0.00 |0.00 |
8213840    |p10201605    |0             |1             |2          |NORMAL|cost_source |[types: [INT]; keys: [10201605]; ..types: [INT]; keys: [10201606]; )|cost_source    |24     |0B      |0       |true           |false     |0.00 |0.00 |0.00 |

```


查看表的tablet 大小及分布情况
show tablet from sr_hr_cryptic_dwd.dwd_hr_cost_empl_detail_1dfsp;

```text
TabletId|BackendId |DataSize|RowCount|
--------+----------+--------+--------+
8215470 |[10745704]|0B      |0       |
8215471 |[10745706]|0B      |0       |
8215472 |[10745706]|0B      |0       |
8215473 |[10745706]|0B      |0       |
8215474 |[10745706]|0B      |0       |
```

## 集群运维
starrocks监控配置
https://starrocks.feishu.cn/docx/ZwkcdCl00oHMMFxNwK7cg20Bnje
集群日常运维技巧
https://docs.starrocks.io/zh/docs/3.2/administration/management/resource_management/Memory_management/#%E6%9F%A5%E7%9C%8B%E5%86%85%E5%AD%98%E4%BD%BF%E7%94%A8
分析BE内存
https://tyd1b38vad.feishu.cn/docx/IMygdVWLboJ4ILx44RwcPe74n3e


## 查看进程是否被系统 kill
grep -i "error\|kill\|segfault" /var/log/messages
dmesg -T | grep 'Out of memory'
dmesg -T





--------------------------------

当前环境开启了算子落盘。但是算子落盘的路径，是和cache 本地盘使用同一块盘，可能会存在spill 数据占用过多磁盘空间的问题
建议分开单独为spill 划分一块盘，或者升级至3.2 最新版本或更高，升级后能支持通过参数  spill_max_dir_bytes_ratio 调整 spill 落盘占用磁盘大小，当前参数默认是0.8，也就是占用磁盘空间的80%，但如果是一直和数据cache 盘使用一块盘的话，需要将参数调整为0.5左右

●set global cbo_enable_low_cardinality_optimize=false;



集群资源使用正常，主要存在以下几个问题：
-   (P0) 当前版本存在grpc 慢锁以及部分外表查询慢锁问题，对系统稳定性存在很大影响，建议尽快升级版本至3.2.15
- （P0）表的分桶大小设置不合理，对象存储存在过多小文件。 存在很多tablet 过多单个tablet 数据量很小的表，会有小文件过多风险。并发扫描时会占用更多的io，业务繁忙的时候影响整体的性能，对元数据的管理来说也不是很合理；存在单bucket的分区，存在一些空数据的表。 建议bucket至少与be数量保持一致。参考3.6.2整改。
-   (P1)  max_map_count 参数需要调整。在机器内存比较大时，如果该参数比较小，可能会引发be crash 问题。 参考3.1.2 修改
-   (P1)   未采用manager 的监控告警，starrocks 服务缺少必要的监控指标和告警配置，需要添加
- （P2）FE 有一个节点混布manager，需要关注FE机器内存




### 作业优化建议规范：
#### 通用：
1. sql中不要使用with sql，with比较耗内存；尽量使用中间表代替；
2. 关联查询时尽量将条件放在on条件中； 先过滤数据再关联提升计算效率；
3. select 和where中不要再次使用select 作为过滤条件；
4. 多个 insert 在同一作业中， 调度单元范围太大，其中一个 insert sql 失败重跑需要全部重跑，比较特殊的有10几个insert


#### Starrocks:
1. 合理使用bukets，选择高基数（枚举值较多）的列作为buket键，buket数量应>5 ，评估表中平均每个分区数据量按100MB一个buket估算
2. partition列与bukets列不要使用同样列，这样就失去优化分区查询效率的目的了；
3. 里面存在重复使用 hive_catalog 同一表，建议把多次使用的hive_catalog表转为SR内部表，减少从hive拉取数据、降低内存消耗提升效率

#### EMR&Hive&Spark



### 需求：
1. datax 中链接mysql oracle等数据源失败时未提示链接失败日志（凯风有遇到），容易迷惑datax任务为什么失败
2. datax sr stream load 时fe未响应时没有重试和超时失败提示
3. dolphin中解析datax日志从中获取本次更新数据行数用于判断波动性



### Starrocks参数：
starlet_star_cache_disk_size_percent=60
配置be.conf starlet仅能使用60%
SET enable_populate_block_cache = false;   开启 Data Cache 后，StarRocks 会缓存从外部存储系统读取的数据文件。如不希望缓存某些数据
SET enable_scan_block_cache = true;
be配置
block_cache_enable
block_cache_disk_size


starlet_use_star_cache = false   如需永久禁用 Data Cache，需要将以下配置添加到 CN 配置文件 cn.conf 中，并重新启动 CN 节点：
set enable_wait_dependent_event = true;  这个参数是代表 执行计划中的某些节点（通常是下游节点）在等待上游数据时，会主动挂起等待，直到依赖的事件触发后再唤醒继续执行。可以减少无效等待和资源占用。但会导致内存资源增加



sql执行计划资料
SQL MPP分布式执行框架
https://docs.starrocks.io/zh/docs/introduction/Features/#mpp-%E5%88%86%E5%B8%83%E5%BC%8F%E6%89%A7%E8%A1%8C%E6%A1%86%E6%9E%B6

各种表引擎使用方式
https://docs.starrocks.io/zh/docs/table_design/data_distribution/


CBO统计信息
采集表基础统计信息
https://docs.starrocks.io/zh/docs/category/cbo-statistics/
https://docs.starrocks.io/zh/docs/sql-reference/sql-statements/cbo_stats/ANALYZE_TABLE/
https://docs.starrocks.io/zh/docs/sql-reference/sql-statements/cbo_stats/CREATE_ANALYZE/

从 v3.3.9 开始，StarRocks 支持将 Trino SQL 语句转换为 StarRocks SQL 语句。
https://docs.starrocks.io/zh/docs/sql-reference/sql-statements/TRANSLATE_TRINO/

查看执行计划
https://docs.starrocks.io/zh/docs/sql-reference/sql-statements/cluster-management/plan_profile/EXPLAIN/

通过执行计划id 或 sql 分析sql执行性能信息
https://docs.starrocks.io/zh/docs/sql-reference/sql-statements/cluster-management/plan_profile/EXPLAIN_ANALYZE/
https://docs.starrocks.io/zh/docs/sql-reference/sql-statements/cluster-management/plan_profile/ANALYZE_PROFILE/


profile结构
Summary： 为资源总使用情况，里面包含开始结束时间，运行耗时

Planner： 生成执行计划耗时情况
Optimizer： 执行计划优化耗时
Scheduler： 调度耗时

Execution： 计划执行过程，耗时及资源使用情况
Topology： 执行计划拓扑图
Fragment： 执行计划中根据聚合、join