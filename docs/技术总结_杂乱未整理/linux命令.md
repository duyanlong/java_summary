
### 增加用户
```shell
# 无登录权限账号
useradd -d /home/hdp-test -s /sbin/nologin -G hdp -m hdp-test
# 有登录权限账号
useradd -d /home/hdp-test -G hdp -m hdp-test
# 添加sudo su 权限， 在/etc/sudoers.d/tme-users中添加下面信息，运行切换到hdp组账号
xxxx ALL=(ALL:hdp) NOPASSWD:ALL
# 允许 xxxx用户登录hdp账号
xxxx ALL=(ALL)  NOPASSWD:/usr/bin/su kamxiao
```

### 查看域名映射地址
dig idata.tmeoa.com
用来检查idata.tmeoa.com域名映射地址是否正确

### 修改ssh端口
```shell
# 在其中修改Port为指定端口
vi /etc/ssh/sshd_config
# 重启sshd，生效
systemctl restart sshd.service
# 查看sshd启动端口
netstat -ntlp|grep ssh
```

* 生成 sha256 hash
```shell 

PASSWORD=$(base64 < /dev/urandom | head -c8);
echo "$PASSWORD"; echo -n "$PASSWORD" | sha256sum | tr -d '-'

```

### 补数技巧
* 生成距今360的所有日期写入文件， 如20210101、20210102
for idx in `seq 0 20` ; do echo `date +'%Y%m%d' -d "-$(expr 20 - ${idx}) day"` >> date.txt ; done
* 根据作业id和日期循环执行作业，补数；但不易控制
nohup for idx in $(cat 31.txt) ; do python3 /data/calc/bin/run_job.py 31 $idx >> 31.log; done 2>&1 &
* 脚本方式根据作业id和日期批量跑作业 参数1：作业id  参数2：日期文件
```shell
#!/bin/bash

for idx in $(cat $2)  
do
 	echo "执行作业日期 $1 $idx "  
	echo "python3 /data/calc/bin/run_job.py $1 $idx >> ${1}.log 2>&1 "
	python3 /data/calc/bin/run_job.py $1 $idx  2>&1 

done
```

* 将日期文件拆分成多个日期文件，用以并行执行，如将20210101-20211230 拆分为20210101-20210630 和20210701-20211230两部分
```shell
sed -n 1,80p date.txt >> date_001.txt
```

### 删除jar中class文件命令
通过缓解方式删除log4j-core包中漏洞类
```shell
zip -q -d log4j-core-*.jar org/apache/logging/log4j/core/lookup/JndiLookup.class
```

### 挂载CFS
* 生产环境
```shell      
mount -t nfs -o vers=4.0,noresvport xx.xx.xxx.xx:/   /data/cfs      
# starrocks 数仓
mount -t nfs -o vers=3,nolock,proto=tcp,noresvport xx.xx.xxx.xx:/4isegjkv/starrocks /data/cfs 
```

* 开发测试环境
```shell  
# 平台开发环境
mount -t nfs -o vers=4.0,noresvport xx.xx.xxx.xx:/  /data/cfs

# uat测试环境
mount -t nfs -o vers=4.0,noresvport xx.xx.xxx.xx:/ /data/cfs
# uat开发环境
mount -t nfs -o vers=4.0,noresvport xx.xx.xxx.xx:/ /data/cfs
```

### 卸载CFS
```shell
umount -l /data/cfs
```

### Airflow集群启动
```shell
docker-compose up -d
```

### hadoop要修改的
```shell
chmod 777 -R /data/emr/*/tmp        
chmod 777 -R /data/emr/*/pid        
chmod 777 -R /data/emr/*/logs   
```


### 升级emr-spark 为apache spark 3.1.2
```shell
scp -r xxxx@xx.xx.xxx.xx:/usr/local/service/spark /usr/local/service/spark_bak
dyl3322207163

mv /usr/local/service/spark_bak/conf /usr/local/service/spark_bak/conf_bak
cp -r /usr/local/service/spark/conf /usr/local/service/spark_bak/
mv /usr/local/service/spark /usr/local/service/spark_bak_emr
mv /usr/local/service/spark_bak /usr/local/service/spark
chown -R hadoop:hadoop /usr/local/service/spark
chown -R hadoop:hadoop /usr/local/service/spark_bak_emr
```

### mvn 
数据计算编译命令，-P可切换环境        
mvn clean package -DskipTests -P prod
数据传输编译命令
mvn clean package -DskipTests assembly:assembly       

### java json
```text
如果使用Hibernate, 查询出重复的数据或者使用类似下面的数据

User s = new User();
s.setAccount("2121");
List<User> list = new ArrayList<User>();
list.add(s);
list.add(s);
System.out.println(JSON.toJSONString(list));
运行结果是：

[{"account":"2121"},{"$ref":"$[0]"}]

如果接口返回上面的数据， 客户端解析数据时会出现问题， 为了避免 $ref出现， 可以使用下面的代码：

JSON.toJSONString(list, SerializerFeature.DisableCircularReferenceDetect)

```

### git

从远程分支创建本地分支
git checkout -b fenzhi001 origin/fenzhi001

列出所有tag     
git tag     
创建tag       
git tag -a release_20210811_synch_dev -m "clickhouse 条件覆盖代码目前没和到master，暂时使用synch_dev代码"     
提交tag       
git push origin release_20210811_synch_dev      

强制覆盖本地      
1.我想将test分支上的代码完全覆盖dev分支，首先切换到dev分支     
git checkout dev        
2.然后直接设置代码给远程的test分支上的代码        
git reset --hard origin/test        
3.执行上面的命令后dev分支上的代码就完全被test分支上的代码覆盖了，注意只是本地分支，这时候还需要将本地分支强行推到远程分支。      
git push -f     

 git fetch --all
 git reset --hard origin/master
 git pull

### docker
从配置文件初始化docker镜像        
docker-compose -f docker-compose-dev.yaml up airflow-init       
从配置文件后台启动docker     
docker-compose -f docker-compose-dev.yaml up -d     

停止docker镜像运行        
docker-compose -f docker-compose-dev.yaml down      

查看docker有哪些运行中镜像        
docker ps       

进入docker        
docker exec -it **** /bin/bash      

查看docker运行日志        
docker logs -f docker_ps_id  

### 批量修改作业所属项目账号（租户）
```sql
-- 平台测试项目账号作业
update calc_db.PGM_BATCH_JOB_INFO set TENANT_ID = 10002 where JOB_INFO like '%HDP_TEST%' or JOB_INFO like '%hdp_test%' or JOB_INFO like '%hdp-test%';
update calc_db.PGM_BATCH_JOB_RUN_HISTORY set TENANT_ID = 10002 where JOB_INFO like '%HDP_TEST%' or JOB_INFO like '%hdp_test%' or JOB_INFO like '%hdp-test%';
-- HR领域项目账号作业
update calc_db.PGM_BATCH_JOB_INFO set TENANT_ID = 10003 where JOB_INFO like '%HDP_HR%' or JOB_INFO like '%hdp_hr%' or JOB_INFO like '%hdp-hr%';
update calc_db.PGM_BATCH_JOB_RUN_HISTORY set TENANT_ID = 10003 where JOB_INFO like '%HDP_HR%' or JOB_INFO like '%hdp_hr%' or JOB_INFO like '%hdp-hr%';
-- QA领域项目账号作业
update calc_db.PGM_BATCH_JOB_INFO set TENANT_ID = 10004 where JOB_INFO like '%HDP_QA%' or JOB_INFO like '%hdp_qa%' or JOB_INFO like '%hdp-qa%';
update calc_db.PGM_BATCH_JOB_RUN_HISTORY set TENANT_ID = 10004 where JOB_INFO like '%HDP_QA%' or JOB_INFO like '%hdp_qa%' or JOB_INFO like '%hdp-qa%';
-- CAS领域项目账号作业
update calc_db.PGM_BATCH_JOB_INFO set TENANT_ID = 10005 where JOB_INFO like '%HDP_CAS%' or JOB_INFO like '%hdp_cas%' or JOB_INFO like '%hdp-cas%';
update calc_db.PGM_BATCH_JOB_RUN_HISTORY set TENANT_ID = 10005 where JOB_INFO like '%HDP_CAS%' or JOB_INFO like '%hdp_cas%' or JOB_INFO like '%hdp-cas%';
-- 财经DASHBoard领域项目账号作业
update calc_db.PGM_BATCH_JOB_INFO set TENANT_ID = 10006 where JOB_INFO like '%HDP_FIN_DASH%' or JOB_INFO like '%hdp_fin_dash%' or JOB_INFO like '%hdp-fin-dash%';
update calc_db.PGM_BATCH_JOB_RUN_HISTORY set TENANT_ID = 10006 where JOB_INFO like '%HDP_FIN_DASH%' or JOB_INFO like '%hdp_fin_dash%' or JOB_INFO like '%hdp-fin-dash%';

-- 其他未明显标记领域项目账号的作业
update calc_db.PGM_BATCH_JOB_INFO pjob set TENANT_ID =10001  where id not in (
select distinct id from  (
select ID from calc_db.PGM_BATCH_JOB_INFO where JOB_INFO like '%HDP_TEST%' or JOB_INFO like '%hdp_test%' or JOB_INFO like '%hdp-test%'
union all
select ID from calc_db.PGM_BATCH_JOB_INFO where JOB_INFO like '%HDP_HR%' or JOB_INFO like '%hdp_hr%' or JOB_INFO like '%hdp-hr%'
union all
select ID from calc_db.PGM_BATCH_JOB_INFO where JOB_INFO like '%HDP_QA%' or JOB_INFO like '%hdp_qa%' or JOB_INFO like '%hdp-qa%'
union all
select ID from calc_db.PGM_BATCH_JOB_INFO where JOB_INFO like '%HDP_CAS%' or JOB_INFO like '%hdp_cas%' or JOB_INFO like '%hdp-cas%'
union all
select ID from calc_db.PGM_BATCH_JOB_INFO where JOB_INFO like '%HDP_FIN_DASH%' or JOB_INFO like '%hdp_fin_dash%' or JOB_INFO like '%hdp-fin-dash%'
) b );
update calc_db.PGM_BATCH_JOB_RUN_HISTORY pjob set TENANT_ID =10001  where JOB_ID not in (
select distinct JOB_ID from  (
select JOB_ID from calc_db.PGM_BATCH_JOB_RUN_HISTORY where JOB_INFO like '%HDP_TEST%' or JOB_INFO like '%hdp_test%' or JOB_INFO like '%hdp-test%'
union all
select JOB_ID from calc_db.PGM_BATCH_JOB_RUN_HISTORY where JOB_INFO like '%HDP_HR%' or JOB_INFO like '%hdp_hr%' or JOB_INFO like '%hdp-hr%'
union all
select JOB_ID from calc_db.PGM_BATCH_JOB_RUN_HISTORY where JOB_INFO like '%HDP_QA%' or JOB_INFO like '%hdp_qa%' or JOB_INFO like '%hdp-qa%'
union all
select JOB_ID from calc_db.PGM_BATCH_JOB_RUN_HISTORY where JOB_INFO like '%HDP_CAS%' or JOB_INFO like '%hdp_cas%' or JOB_INFO like '%hdp-cas%'
union all
select JOB_ID from calc_db.PGM_BATCH_JOB_RUN_HISTORY where JOB_INFO like '%HDP_FIN_DASH%' or JOB_INFO like '%hdp_fin_dash%' or JOB_INFO like '%hdp-fin-dash%'
) b );

```
### 批量修改hdfs目录权限
```shell
初次搭建集群初始化hdfs目录
hadoop fs -mkdir -p /user/hdp-hr
hadoop fs -mkdir -p /user/hdp-qa
hadoop fs -mkdir -p /user/hdp-test
hadoop fs -mkdir -p /user/hdp-fin-dash
hadoop fs -mkdir -p /user/hdp-inc
hadoop fs -mkdir -p /data/hdp-fin-dash
hadoop fs -mkdir -p /data/hdp-hr
hadoop fs -mkdir -p /data/hdp-qa
hadoop fs -mkdir -p /data/hdp-test
hadoop fs -mkdir -p /data/hdp-inc

默认授权hdfs目录
hadoop fs -chown -R hdp-hr:hdp /user/hdp-hr
hadoop fs -chown -R hdp-qa:hdp /user/hdp-qa
hadoop fs -chown -R hdp-test:hdp /user/hdp-test
hadoop fs -chown -R hdp-fin-dash:hdp /user/hdp-fin-dash
hadoop fs -chown -R hdp-inc:hdp /user/hdp-inc
hadoop fs -chown -R hdp-fin-dash:hdp /data/hdp-fin-dash
hadoop fs -chown -R hdp-hr:hdp /data/hdp-hr
hadoop fs -chown -R hdp-qa:hdp /data/hdp-qa
hadoop fs -chown -R hdp-test:hdp /data/hdp-test
hadoop fs -chown -R hdp-inc:hdp /data/hdp-inc
hadoop fs -chown -R hdp-fin-dash:hdp /usr/hive/warehouse/hdp_fin_dash*
hadoop fs -chown -R hdp-hr:hdp /usr/hive/warehouse/hdp_hr*
hadoop fs -chown -R hdp-qa:hdp /usr/hive/warehouse/hdp_qa*
hadoop fs -chown -R hdp-test:hdp /usr/hive/warehouse/hdp_test*
hadoop fs -chown -R hdp-inc:hdp /usr/hive/warehouse/hdp_inc*

规范权限
hadoop fs -chmod  -R 755 /user/hdp-hr/*
hadoop fs -chmod  -R 755 /user/hdp-qa/*
hadoop fs -chmod  -R 755 /user/hdp-test/*
hadoop fs -chmod  -R 755 /user/hdp-fin-dash/*
hadoop fs -chmod  -R 755 /user/hdp-inc/*
hadoop fs -chmod  -R 755 /data/hdp-fin-dash/*
hadoop fs -chmod  -R 755 /data/hdp-hr/*
hadoop fs -chmod  -R 755 /data/hdp-qa/*
hadoop fs -chmod  -R 755 /data/hdp-test/*
hadoop fs -chmod  -R 755 /data/hdp-inc/*
hadoop fs -chmod  -R 755 /usr/hive/warehouse/hdp_fin_dash/*
hadoop fs -chmod  -R 755 /usr/hive/warehouse/hdp_hr/*
hadoop fs -chmod  -R 755 /usr/hive/warehouse/hdp_qa/*
hadoop fs -chmod  -R 755 /usr/hive/warehouse/hdp_test/*
hadoop fs -chmod  -R 755 /usr/hive/warehouse/hdp_inc/*
```

## calcServer DB调整SQL
### 修复PGM_BATCH_JOB_INFO表JOB_INFO信息
```sql
update
	PGM_BATCH_JOB_INFO t1
join (
	select
		distinct chk_sum,
		job_info,
		JOB_PARAM
	from
		PGM_BATCH_JOB_RUN_HISTORY ) t2 on
	t1.CHK_SUM = t2.chk_sum set
	t1.JOB_INFO = t2.job_info ,
	t1.JOB_PARAM = t2.job_param
```

### 运营月报
统计作业总数、运行时长30min内作业数量、作业运行总次数、运行时长30min内作业占比、一次成功(一天内运行结束),一次成功率
```sql
select
	count(distinct JOB_ID) as task_dis_cnt,
	COUNT(case when num_time >= 0 and num_time<30 then 1 else null end) task_30m ,
	COUNT(1) task_cnt ,
	COUNT(case when num_time >= 0 and num_time<30 then 1 else null end)* 100 / COUNT(1) as task_30m_rate,
	count(case when num_time<1520 then 1 else null end) as first_success ,
	count(case when num_time<1520 then 1 else null end)* 100 /COUNT(1) as first_success_rate 
from
	(
	select
		UPDATE_DATE ,
		INSERT_DATE ,
		TIMESTAMPDIFF(MINUTE ,
		INSERT_DATE,
		UPDATE_DATE) as num_time,
		ENGINE_TYPE,
		JOB_ID
	from
		calc_db.PGM_BATCH_JOB_RUN_HISTORY pbjrh
	where
		RUN_STATE >2
		and INSERT_DATE > '2021-01-01 00:00:00.0'
		and INSERT_DATE < '2022-03-01 00:00:00.0' ) as tbla 
```


## 登录服务器
* 登录DevSecOps XABC平台 http://relay.tmeoa.com/login?next=%2F#/accessapply 权限-私钥申领
* 登录DevSecOps XABC平台 权限-权限申请，  将要申请的服务器输入进行申请权限
* ssh 10.135.0.13登录测试堡垒机  ssh 10.135.8.12登录生产堡垒机（开发不用申请）  需要sudo权限需要勾选sudo，默认 normal
* 登录堡垒机后会有提示：
 ```text
suncky:5467   jianjunhu:3508   jammyzhang:3392   allanfjchen:3011   helv:2913   linlingqiang:2587   kyrieding:2499     
 xxxx:39        Local(本地) ---> Login(南天门) ---> Host(业务机器)
 2022-01-17)	   $ssh 01 ---> NEW-xx.xx.xxx.xx-bj
 2022-02-10)	   $ssh 02 ---> CALC-xx.xx.xxx.xx-gz
 2022-02-10)	   $ssh 03 ---> CALC-xx.xx.xxx.xx-gz
 2022-02-10)	   $ssh 04 ---> CALC-xx.xx.xxx.xx-gz
==================================================================================================================================================================
 在很特殊的情况下,一个人才会成为圣人,但做一个正直的人却是人生的正轨.尽管你们曾经犯错,曾经迷惘,你们也应当尽自己最大的能力去做一个正直的人
==================================================================================================================================================================
 ```
 其中执行命令  ssh 02  是登录到服务器  xx.xx.xxx.xx 上，登录后和现在操作步骤一样~~~~

 ## airflow加权限
 修改ab_use、ab_user_role两张表
 
 ## 中台添加新租户
 * sql
 ```sql
 INSERT INTO calc_db.RES_TENANT
 (ID, TENANT_NAME, YARN_QUEUE, STOREAGE_USER, IS_ACTIVE, INSERT_USER, INSERT_DATE, UPDATE_USER, UPDATE_DATE, MARK)
 VALUES(10007, 'hdp-inc', 'default', 'hdp-inc', 1, 'xxxx', '2022-04-06 15:38:46.0', 'xxxx', '2022-04-06 15:38:46.0', NULL);
 ```
* 系统账号添加和hadoop目录建立
```shell
 useradd hdp-inc
 hadoop fs -mkdir -p /data/hdp-inc/warehouse/hdp_inc_ods 
 hadoop fs -mkdir -p /data/hdp-inc/warehouse/hdp_inc_dwd
 hadoop fs -mkdir -p /data/hdp-inc/warehouse/hdp_inc_dws
 hadoop fs -mkdir -p /data/hdp-inc/warehouse/hdp_inc_ads 
 hadoop fs -chown -R hdp-inc:hdp /data/hdp-inc
```
hive 库表建立、clickhouse库表建立自行创建即可

## hive新集群跟进 hdfs目录结构修复表分区
在Hive中，可以使用MSCK REPAIR TABLE命令修复表中缺失的分区。以下是MSCK REPAIR TABLE命令的语法：

复制
MSCK REPAIR TABLE table_name;
在这个命令中，table_name是要修复的表的名称。

MSCK REPAIR TABLE命令的原理是扫描表的默认HDFS路径，查找缺失的分区，并将它们添加到表中。如果表的默认HDFS路径中存在未注册的分区，则MSCK REPAIR TABLE命令将自动将这些分区添加到表中。

以下是一个示例命令，可以修复名为abc的表中缺失的分区：

复制
MSCK REPAIR TABLE abc;
请注意，在使用MSCK REPAIR TABLE命令时，需要确保表的默认HDFS路径正确，并且缺失的分区存储在正确的HDFS路径中。另外，如果表的分区列较多，则需要确保分区列的名称和数据类型与表的定义相匹配。

## 按照前端 npm
yum install npm
npm install
npm install -g pnpm

## 前端 nvm   node version manager 安装使用
安装  
https://blog.csdn.net/weixin_46516647/article/details/130108878
管理使用  
https://zhuanlan.zhihu.com/p/646970780?utm_id=0
dolphinscheduler 和 dinky 需要切换 nvm版本 
nvm use v20.11.0
nvm ls  查看有哪些可使用版本
nvm install <version>  安装版本
node -v   查看版本
npm install
npm install -g yarn
yarn install

## linux 删除 3 天前文件和目录

find /path/to/directory -type f -mtime +3 -exec rm -f {} \;
2.
find /data/emr/hive/tmp/ -type f -mtime +3 | xargs rm -f
3.  
find data//emr/hive/tmp/ -type f -mtime +3 -delete
find /data/emr/hive/tmp/ -type d -empty -delete


## 清理 linux 系统缓存
sudo sh -c 'echo 3 > /proc/sys/vm/drop_caches'

## xx.xx.xxx.xx 重启 hiveserver2 
hive --service hiveserver2 --hiveconf hive.server2.thrift.port=7001 > /data/emr/hive/logs/hadoop-hive 2>&1 &

## 程序异常重启，排查是否机器层面资源导致
grep -E "starrocks" /var/log/syslog /var/log/messages|grep -Ev 'Started'
```text
grep: /var/log/syslog: No such file or directory
/var/log/messages:Sep 26 19:25:13 localhost kernel: [40479]  1003 40479 45363873 15812849   72079        0             0 starrocks_be
/var/log/messages:Sep 26 19:25:13 localhost kernel: Out of memory: Kill process 40479 (starrocks_be) score 965 or sacrifice child
/var/log/messages:Sep 26 19:25:13 localhost kernel: Killed process 40479 (starrocks_be), UID 1003, total-vm:181455492kB, anon-rss:63251396kB, file-rss:0kB, shmem-rss:0kB
/var/log/messages:Sep 26 19:25:20 localhost systemd: Removed slice User Slice of starrocks.
/var/log/messages:Sep 26 19:26:01 localhost systemd: Created slice User Slice of starrocks.
/var/log/messages:Dec 20 19:25:13 localhost kernel: [11191]  1003 11191 39623475 15830263   67141        0             0 starrocks_be
/var/log/messages:Dec 20 19:25:13 localhost kernel: Out of memory: Kill process 11191 (starrocks_be) score 966 or sacrifice child
/var/log/messages:Dec 20 19:25:13 localhost kernel: Killed process 11191 (starrocks_be), UID 1003, total-vm:158493900kB, anon-rss:63321052kB, file-rss:0kB, shmem-rss:0kB
/var/log/messages:Dec 20 19:25:20 localhost systemd: Removed slice User Slice of starrocks.
/var/log/messages:Dec 20 19:26:01 localhost systemd: Created slice User Slice of starrocks.
```
