[TOC]

## 基于kerberos数据中台验证

本机虚拟机集群配置

| master(192.168.1.167)                            | slaver1(192.168.1.168) | slaver2(192.168.1.169) |
| ------------------------------------------------ | ---------------------- | ---------------------- |
| NameNode                                         | NodeManager            | DataNode               |
| NodeManager                                      | DataNode               | NodeManager            |
| JobHistoryServer                                 | hive client            | ResourceManager        |
| hive metastore                                   | spark client           | hive client            |
| hiveserver2                                      | kdc client             | spark client           |
| hive client                                      |                        | kdc client             |
| spark client                                     |                        |                        |
| kdc server                                       |                        |                        |
| kdc client                                       |                        |                        |
| ranger-admin，hdfs-plugin，hive-plugin，usersync |                        |                        |

kerberos 架构

https://www.cnblogs.com/wukenaihe/p/3732141.html

#### 安装kerberos

安装过程参考

https://www.cnblogs.com/bainianminguo/p/12548076.html

https://blog.csdn.net/qq_40341628/article/details/84990148?spm=1001.2014.3001.5501

master 安装 KDC server

```
yum install krb5-workstation krb5-libs krb5-auth-dialog krb5-server
```

修改配置文件

/etc/krb5.conf 		

```
# Configuration snippets may be placed in this directory as well
includedir /etc/krb5.conf.d/

[logging]
 default = FILE:/var/log/krb5libs.log
 kdc = FILE:/var/log/krb5kdc.log
 admin_server = FILE:/var/log/kadmind.log

[libdefaults]
 dns_lookup_realm = false
 ticket_lifetime = 999d #默认24h 生效时间
 renew_lifetime = 999d #默认7d 可延长的生效期限
 forwardable = true
 rdns = false
 pkinit_anchors = FILE:/etc/pki/tls/certs/ca-bundle.crt
 default_realm = HADOOP.COM
# default_ccache_name = KEYRING:persistent:%{uid}
[realms]
 HADOOP.COM = {
 kdc = master
 admin_server = master
 }

[domain_realm]
# .example.com = EXAMPLE.COM
# example.com = EXAMPLE.COM
```

/var/kerberos/krb5kdc/kdc.conf

```
[kdcdefaults]
 kdc_ports = 88
 kdc_tcp_ports = 88

[realms]
 HADOOP.COM = {
  master_key_type = aes128-cts
  acl_file = /var/kerberos/krb5kdc/kadm5.acl
  dict_file = /usr/share/dict/words
  admin_keytab = /var/kerberos/krb5kdc/kadm5.keytab
  supported_enctypes = aes128-cts:normal des3-hmac-sha1:normal arcfour-hmac:normal camellia256-cts:normal camellia128-cts:normal des-hmac-sha1:normal des-cbc-md5:normal des-cbc-crc:normal
  max_life = 999d
  max_renewable_life = 999d
}
```

默认aes256-cts:normal 算法需要另准备jar包，已移除，设置使用aes128-cts

初始化kdc 数据库

```
[root@master krb5kdc]# kdb5_util create -r HADOOP.COM -s 
Loading random data
Initializing database '/var/kerberos/krb5kdc/principal' for realm 'HADOOP.COM',
master key name 'K/M@HADOOP.COM'
You will be prompted for the database Master Password.
It is important that you NOT FORGET this password.
Enter KDC database master key: 
Re-enter KDC database master key to verify: 
```

重新初始化kdc数据库：

先删除已验证的ticket：kdestroy

再删除/var/kerberos/krb5kdc/目录下principal principal.kadm5 principal.kadm5.lock principal.ok 4个文件

再执行kdb5_util create -s -r 'HADOOP.COM'

设置服务开机自启动

```
systemctl enable krb5kdc.service
systemctl enable kadmin.service
systemctl restart krb5kdc.service
systemctl restart kadmin.service
```

添加需要验证的用户主体

```
[root@master krb5kdc]# kadmin.local
kadmin.local:  addprinc hadoop/master
WARNING: no policy specified for hadoop/master@HADOOP.COM; defaulting to no policy
Enter password for principal "hadoop/master@HADOOP.COM": 
Re-enter password for principal "hadoop/master@HADOOP.COM": 
Principal "hadoop/master@HADOOP.COM" created.
kadmin.local:  addprinc hadoop/slaver1
WARNING: no policy specified for hadoop/slaver1@HADOOP.COM; defaulting to no policy
Enter password for principal "hadoop/slaver1@HADOOP.COM": 
Re-enter password for principal "hadoop/slaver1@HADOOP.COM": 
Principal "hadoop/slaver1@HADOOP.COM" created.
kadmin.local:  addprinc hadoop/slaver2
WARNING: no policy specified for hadoop/slaver2@HADOOP.COM; defaulting to no policy
Enter password for principal "hadoop/slaver2@HADOOP.COM": 
Re-enter password for principal "hadoop/slaver2@HADOOP.COM": 
Principal "hadoop/slaver2@HADOOP.COM" created.
kadmin.local:  addprinc aaa/master
WARNING: no policy specified for aaa/master@HADOOP.COM; defaulting to no policy
Enter password for principal "aaa/master@HADOOP.COM": 
Re-enter password for principal "aaa/master@HADOOP.COM": 
Principal "hadoop/master@HADOOP.COM" created.
```

启动客户端验证票据

kinit命令用于获取和缓存principal（当前主体）初始的票据授予票据（TGT）

klist查看当前主体票据

```
[hadoop@master krb5kdc]$ kinit hadoop/master
Password for hadoop/master@HADOOP.COM: 
[hadoop@master krb5kdc]$ klist -e
Ticket cache: FILE:/data/krb5cccache/krb5cc_5149
Default principal: hadoop/master@HADOOP.COM

Valid starting       Expires              Service principal
11/04/2021 14:59:56  07/30/2024 14:59:56  krbtgt/HADOOP.COM@HADOOP.COM
        Etype (skey, tkt): aes128-cts-hmac-sha1-96, aes128-cts-hmac-sha1-96 
```

生成秘钥文件验证

```
kadmin.local:  xst -norandkey -k /data/hadoop.keytab hadoop/master@HADOOP.COM hadoop/slaver1@HADOOP.COM hadoop/slaver2@HADOOP.COM aaa/master@HADOOP.COM
Entry for principal hadoop/master@HADOOP.COM with kvno 1, encryption type aes128-cts-hmac-sha1-96 added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal hadoop/master@HADOOP.COM with kvno 1, encryption type des3-cbc-sha1 added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal hadoop/master@HADOOP.COM with kvno 1, encryption type arcfour-hmac added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal hadoop/master@HADOOP.COM with kvno 1, encryption type camellia256-cts-cmac added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal hadoop/master@HADOOP.COM with kvno 1, encryption type camellia128-cts-cmac added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal hadoop/master@HADOOP.COM with kvno 1, encryption type des-hmac-sha1 added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal hadoop/master@HADOOP.COM with kvno 1, encryption type des-cbc-md5 added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal hadoop/slaver1@HADOOP.COM with kvno 1, encryption type aes128-cts-hmac-sha1-96 added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal hadoop/slaver1@HADOOP.COM with kvno 1, encryption type des3-cbc-sha1 added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal hadoop/slaver1@HADOOP.COM with kvno 1, encryption type arcfour-hmac added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal hadoop/slaver1@HADOOP.COM with kvno 1, encryption type camellia256-cts-cmac added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal hadoop/slaver1@HADOOP.COM with kvno 1, encryption type camellia128-cts-cmac added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal hadoop/slaver1@HADOOP.COM with kvno 1, encryption type des-hmac-sha1 added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal hadoop/slaver1@HADOOP.COM with kvno 1, encryption type des-cbc-md5 added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal hadoop/slaver2@HADOOP.COM with kvno 1, encryption type aes128-cts-hmac-sha1-96 added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal hadoop/slaver2@HADOOP.COM with kvno 1, encryption type des3-cbc-sha1 added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal hadoop/slaver2@HADOOP.COM with kvno 1, encryption type arcfour-hmac added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal hadoop/slaver2@HADOOP.COM with kvno 1, encryption type camellia256-cts-cmac added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal hadoop/slaver2@HADOOP.COM with kvno 1, encryption type camellia128-cts-cmac added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal hadoop/slaver2@HADOOP.COM with kvno 1, encryption type des-hmac-sha1 added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal hadoop/slaver2@HADOOP.COM with kvno 1, encryption type des-cbc-md5 added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal aaa/master@HADOOP.COM with kvno 1, encryption type aes128-cts-hmac-sha1-96 added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal aaa/master@HADOOP.COM with kvno 1, encryption type des3-cbc-sha1 added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal aaa/master@HADOOP.COM with kvno 1, encryption type arcfour-hmac added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal aaa/master@HADOOP.COM with kvno 1, encryption type camellia256-cts-cmac added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal aaa/master@HADOOP.COM with kvno 1, encryption type camellia128-cts-cmac added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal aaa/master@HADOOP.COM with kvno 1, encryption type des-hmac-sha1 added to keytab WRFILE:/data/hadoop.keytab.
Entry for principal aaa/master@HADOOP.COM with kvno 1, encryption type des-cbc-md5 added to keytab WRFILE:/data/hadoop.keytab.
```

多个用户主体可以共同使用一个秘钥，生成秘钥文件后，原来密码验证就会失效

使用秘钥文件重新验证

说明：root用户下生成的秘钥文件只有root 读写权限，切换其他用户验证使用秘钥文件验证主体会报kinit: Permission denied while getting initial credentials异常。可以使用chmod 提升其他用户对秘钥文件的读写权限或者chown命令改变秘钥文件所有者，然后再初始化

```
chmod 644 /data/hadoop.keytab
kinit -kt /data/hadoop.keytab hadoop/master
klist -e
```

其他节点安装client

```
yum install krb5-workstation krb5-libs krb5-auth-dialog -y
```

从kdc server端同步配置文件和秘钥文件

```
sudo scp root@master:/etc/krb5.conf /etc
sudo scp root@master:/data/hadoop.keytab /data ;sudo chown hadoop:hadoop /data/hadoop.keytab
```

其他节点启动客户端验证票据

```
kinit -kt /data/hadoop.keytab hadoop/slaver1
kinit -kt /data/hadoop.keytab hadoop/slaver2
```

删除当前的认证缓存

```
kdestroy
```

windows删除认证缓存 

```
 kdestroy或者删除c盘C:\Users\当前用户名\krb5cc_当前用户名 文件
```

更新票据

```
kinit -R 
```

windows kerberos客户端

在Kerberos官网下载Kerberos安装包

https://blog.csdn.net/qq_40341628/article/details/84991443?utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7Edefault-14.no_search_link&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7Edefault-14.no_search_link

windows端也可以使用jdk自带kinit.exe和klist.exe验证票据，windows java已安装，从kdc server端同步配置文件和秘钥文件到windows目录

在C:\Windows目录下新建krb5.ini文档，复制/etc/krb5.conf文本内容粘贴到文档中，注释掉linux路径的相关配置

```
# Configuration snippets may be placed in this directory as well

[logging]
#default = FILE:/var/log/krb5libs.log
#kdc = FILE:/var/log/krb5kdc.log
#admin_server = FILE:/var/log/kadmind.log

[libdefaults]
dns_lookup_realm = false
ticket_lifetime = 999d
renew_lifetime = 999d
forwardable = true
rdns = false
#pkinit_anchors = FILE:/etc/pki/tls/certs/ca-bundle.crt
default_realm = HADOOP.COM
#default_ccache_name = KEYRING:persistent:%{uid}

[realms]
HADOOP.COM = {
kdc = master
admin_server = master
 }

[domain_realm]
# .example.com = EXAMPLE.COM
# example.com = EXAMPLE.COM
```

使用windows MIT kerberos工具

```
cd C:\Program Files\MIT\Kerberos\bin
kinit.exe  -kt "C:\mytemp\hadoop.keytab" hadoop/master@HADOOP.COM
kinit
```

使用java自带kinit验证

```
D:\JAVA\jdk1.8-8u172\bin\kinit.exe -k -t "C:\mytemp\hadoop.keytab" hadoop/master@HADOOP.COM
D:\JAVA\jdk1.8-8u172\bin\klist.exe
```



#### 配置hadoop on kerberos

hadoop启动各组件通信需要验证kerberos，可以给每个组件分配不同的秘钥，这里使用一个

配置参考

https://docs.cloudera.com/HDPDocuments/HDP2/HDP-2.4.0/bk_Security_Guide/content/create_mappings_betw_principals_and_unix_usernames.html

https://blog.csdn.net/m1213642578/article/details/52450639

https://www.cnblogs.com/niceshot/p/14906696.html

core-site.xml

```
<configuration>
  <property>
        <name>fs.defaultFS</name>
        <value>hdfs://192.168.1.167:4007</value>
    </property>
<!--
<property>
        <name>hadoop.http.staticuser.user</name>
        <value>hadoop</value>
    </property>
-->
<property>
        <name>hadoop.logfile.count</name>
        <value>20</value>
    </property>

    <property>
        <name>hadoop.logfile.size</name>
        <value>1000000000</value>
    </property>

    <property>
        <name>hadoop.proxyuser.hadoop.groups</name>
        <value>*</value>
    </property>

    <property>
        <name>hadoop.proxyuser.hadoop.hosts</name>
        <value>*</value>
    </property>

   <property>
        <name>hadoop.tmp.dir</name>
        <value>/data/hadoop-2.8.5/data</value>
    </property>

   <property>
     <name>hadoop.security.authentication</name>
     <value>kerberos</value>
   </property>
   <property>
     <name>hadoop.security.authorization</name>
     <value>true</value>
   </property>

<property> 
        <name>hadoop.rpc.protection</name> 
        <value>authentication</value> 
</property>

<property> 
        <name>hadoop.security.auth_to_local</name>
        <value>
                RULE:[2:$1@$0](hadoop/.*@HADOOP.COM)s/.*/hadoop/
                DEFAULT
        </value> 
</property> 

<property>
  <name>hadoop.proxyuser.hive.users</name>
  <value>*</value>
</property>
<property>
  <name>hadoop.proxyuser.hive.hosts</name>
  <value>*</value>
</property>

</configuration>
```

hadoop.security.auth_to_local :从访问的Principal中抽取出实际的用户,最终的default是上述规则都不匹配时的默认规则，默认规则会直接从principal中提取第一个斜杠前面的信息作为user。比如test/xxhost@DOMIAN.COM 会被识别成明为test的user

hdfs-site.xml

```

<configuration>

        <property>
                <name>dfs.namenode.http-address</name>
                <value>192.168.1.167:4008</value>
        </property>

        <property>
                <name>dfs.namenode.https-address</name>
                <value>192.168.1.167:4009</value>
        </property>

         <property>
                <name>dfs.namenode.rpc-address</name>
                <value>192.168.1.167:4007</value>
        </property>

        <property>
                <name>dfs.nameservices</name>
                <value>192.168.1.167:4007</value>
        </property>

        <property> 
                <name>dfs.datanode.data.dir.perm</name> 
                <value>700</value> 
        </property> 
        <property> 
                <name>dfs.datanode.address</name> 
                <value>0.0.0.0:4001</value> 
        </property>
        <property> 
                <name>dfs.datanode.http.address</name> 
                <value>0.0.0.0:4002</value> 
        </property>


    <property>
        <name>dfs.namenode.secondary.http-address</name>
        <value>192.168.1.169:4010</value>
    </property>

   <property>
        <name>dfs.webhdfs.enabled</name>
        <value>true</value>
   </property>

        <property>
                <name>dfs.replication</name>
                <value>1</value>
        </property>

 <property>

       <name>dfs.namenode.name.dir</name>
       <value>/data/hadoop-2.8.5/data/namenode</value>

   </property>

   <property>
       <name>dfs.datanode.data.dir</name>
       <value>/data/hadoop-2.8.5/data/datanode</value>
   </property>



<property>
    <name>dfs.block.access.token.enable</name>
    <value>true</value>
</property>

     <property> 
        <name>dfs.encrypt.data.transfer</name> 
        <value>true</value>
        <description>激活为Datanode的数据传输协议的数据加密,激活则需启用sasl</description>
  </property> 
<property>
    <name>dfs.namenode.kerberos.principal</name>
    <value>hadoop/_HOST@HADOOP.COM</value>
</property>
<property>
    <name>dfs.namenode.keytab.file</name>
    <value>/data/hadoop.keytab</value>
</property>
<property>
    <name>dfs.namenode.kerberos.internal.spnego.principal</name>
    <value>hadoop/_HOST@HADOOP.COM</value>
</property>
<property>
    <name>dfs.namenode.kerberos.internal.spnego.keytab</name>
    <value>hadoop/_HOST@HADOOP.COM</value>
</property>
<property>
    <name>dfs.web.authentication.kerberos.principal</name>
    <value>hadoop/_HOST@HADOOP.COM</value>
</property>
<property>
    <name>dfs.web.authentication.kerberos.keytab</name>
    <value>/data/hadoop.keytab</value>
</property>
<property>
    <name>dfs.datanode.kerberos.principal</name>
    <value>hadoop/_HOST@HADOOP.COM</value>
</property>
<property>
    <name>dfs.datanode.keytab.file</name>
    <value>/data/hadoop.keytab</value>
</property>
<property> 
        <name>dfs.datanode.kerberos.https.principal</name> 
        <value>hadoop/_HOST@HADOOP.COM</value> 
 </property>  

<property>
    <name>dfs.secondary.namenode.keytab.file</name>
    <value>/data/hadoop.keytab</value>
</property>
<property>
    <name>dfs.secondary.namenode.kerberos.principal</name>
    <value>hadoop/_HOST@HADOOP.COM</value>
</property>

<property>
        <name>dfs.data.transfer.protection</name>
        <value>integrity</value>
        <description>逗号分隔的SASL保护值列表，用于在读取或写入块数据时与DataNode进行安全连接。可能的值为:"authentication"(仅表示身份验证，没有完整性或隐私), "integrity"(意味着启用了身份验证和完整性)和"privacy"(意味着所有身份验证，完整性和隐私都已启用)。如果dfs.encrypt.data.transfer设置为true，则它将取代dfs.data.transfer.protection的设置，并强制所有连接必须使用专门的加密SASL握手。对于与在特权端口上侦听的DataNode的连接，将忽略此属性。在这种情况下，假定特权端口的使用建立了足够的信任。</description>
</property>
<property>
    <name>dfs.http.policy</name>
    <!--<value>HTTPS_ONLY</value>-->
    <!--<value>HTTP_ONLY</value>-->
   <value>HTTP_AND_HTTPS</value>
        <description>确定HDFS是否支持HTTPS(SSL)。默认值为"HTTP_ONLY"(仅在http上提供服务),"HTTPS_ONLY"(仅在https上提供服务,DataNode节点设置该值),"HTTP_AND_HTTPS"(同时提供服务在http和https上,NameNode和Secondary NameNode节点设置该值)。</description>
</property>

<property>
    <name>dfs.client.use.datanode.hostname</name>
    <value>true</value>
</property>
<property>
        <name>dfs.datanode.use.datanode.hostname</name>
        <value>true</value>
    </property>

</configuration>
```

测试发现datanode 和namenode无法在同一节点启动，namenode hdfs-site.xml dfs.http.policy需设置HTTP_AND_HTTPS，datanode  hdfs-site.xml dfs.http.policy需设置HTTPS_ONLY

datanode启动还需要开启SASL，通过TLS/SSL来实现数据的安全传输，配置hadoop conf 目录下ssl-client.xml.和ssl-server.xml

配置参考

https://www.cnblogs.com/niceshot/p/14906696.html

mapred-site.xml

```
<configuration>

        <property>
                <name>mapreduce.jobhistory.address</name>
                <value>192.168.1.167:5022</value>
        </property>

        <property>
                <name>mapreduce.jobhistory.admin.address</name>
                <value>192.168.1.167:5023</value>
        </property>

        <property>
                <name>mapreduce.jobhistory.webapp.address</name>
                <value>192.168.1.167:5024</value>
        </property>

        <property>
                <name>mapreduce.framework.name</name>
                <value>yarn</value>
        </property>

        <property>
                <name>mapreduce.jobhistory.keytab</name>
                <value>/data/hadoop.keytab</value>
        </property>
        <property>
                <name>mapreduce.jobhistory.principal</name>
                <value>hadoop/_HOST@HADOOP.COM</value>
    </property>

</configuration>
```

yarn-site.xml

```
<configuration>

<!-- Site specific YARN configuration properties -->
        <property>
                <name>yarn.log-aggregation-enable</name>
                <value>true</value>
        </property>

        <property>
                <name>yarn.log-aggregation.retain-check-interval-seconds</name>
                <value>604800</value>
        </property>

        <property>
                <name>yarn.log-aggregation.retain-seconds</name>
                <value>604800</value>
        </property>

        <property>
                <name>yarn.log.server.url</name>
                <value>http://192.168.1.169:5024/jobhistory/logs</value>
        </property>


        <property>
                <name>yarn.nodemanager.address</name>
                <value>0.0.0.0:5006</value>
        </property>

        <property>
                <name>yarn.nodemanager.aux-services</name>
                <value>mapreduce_shuffle</value>
        </property>

        <property>
                <name>yarn.nodemanager.hostname</name>
                <value>0.0.0.0.167</value>
        </property>

        <property>
                <name>yarn.nodemanager.localizer.address</name>
                <value>0.0.0.0:5007</value>
        </property>



        <property>
                <name>yarn.nodemanager.webapp.address</name>
                <value>0.0.0.0:5008</value>
        </property>

        <property>
                <name>yarn.resourcemanager.address</name>
                <value>192.168.1.169:5000</value>
        </property>

        <property>
                <name>yarn.resourcemanager.admin.address</name>
                <value>192.168.1.169:5003</value>
        </property>

         <property>
                <name>yarn.resourcemanager.resource-tracker.address</name>
                <value>192.168.1.169:5002</value>
        </property>

        <property>
                <name>yarn.resourcemanager.scheduler.address</name>
                <value>192.168.1.169:5001</value>
        </property>

        <property>
                <name>yarn.resourcemanager.webapp.address</name>
                <value>192.168.1.169:5004</value>
        </property>

        <property>
                <name>yarn.resourcemanager.webapp.https.address</name>
                <value>192.168.1.169:5005</value>
        </property>
        <property>
                <name>yarn.nodemanager.env-whitelist</name>
                <value>JAVA_HOME,HADOOP_COMMON_HOME,HADOOP_HDFS_HOME,HADOOP_CONF_DIR,CLASSPATH_PREPEND_DISTCACHE,HADOOP_YARN_HOME,HADOOP_MAPRED_HOME</value>
        </property>


        <property>
                <name>yarn.resourcemanager.principal</name>
                <value>hadoop/_HOST@HADOOP.COM</value>
        </property>
        <property>
                <name>yarn.resourcemanager.keytab</name>
                <value>/data/hadoop.keytab</value>
        </property>
        <property>
                <name>yarn.nodemanager.keytab</name>
                <value>/data/hadoop.keytab</value>
        </property>
        <property>
                <name>yarn.nodemanager.principal</name>
                <value>hadoop/_HOST@HADOOP.COM</value>
        </property>

<property>
     <name>yarn.nodemanager.pmem-check-enabled</name>
     <value>false</value>
</property>
<property>
     <name>yarn.nodemanager.vmem-check-enabled</name>
     <value>false</value>
</property>

<property>
        <name>yarn.scheduler.minimum-allocation-mb</name>
        <value>512</value>
    </property>
    <property>
        <name>yarn.scheduler.maximum-allocation-mb</name>
        <value>4096</value>
    </property>
    <property>
        <name>yarn.nodemanager.resource.memory-mb</name>
        <value>4096</value>
</property>

</configuration>
```

#### hadoop kerberos 验证

启动集群

master节点

```
[hadoop@master conf]$ jps
14144 JobHistoryServer
14240 RunJar
13362 NameNode
16468 Jps
13610 NodeManager
14187 RunJar
```

slaver1节点

```
[hadoop@slaver1 ~]$ jps
8788 Jps
7246 NodeManager
7119 DataNode
```

slaver2节点

```
[hadoop@slaver2 ~]$ jps
8992 DataNode
9264 NodeManager
9100 ResourceManager
10956 Jps
```

已注册用户验证

```
[hadoop@slaver2 ~]$ hdfs dfs -mkdir  /test
[hadoop@slaver2 ~]$ hdfs dfs -ls /
Found 7 items
drwxr-xr-x   - hadoop supergroup          0 2021-10-25 01:39 /data
-rw-r--r--   2 hadoop supergroup       2324 2021-10-25 03:13 /hadoop.keytab
drwxr-xr-x   - hadoop supergroup          0 2021-10-25 02:50 /spark-history
drwxr-xr-x   - hadoop supergroup          0 2021-10-26 07:23 /test
drwxrwx---   - hadoop supergroup          0 2021-10-17 20:10 /tmp
drwxr-xr-x   - hadoop supergroup          0 2021-10-14 18:54 /user
drwxr-xr-x   - hadoop supergroup          0 2021-10-17 23:12 /usr
```

无权限用户测试

```
[hadoop@slaver2 ~]$ su ccc
Password: 
[ccc@slaver2 hadoop]$ hdfs dfs -mkdir  /test
21/10/26 07:27:05 WARN ipc.Client: Exception encountered while connecting to the server : javax.security.sasl.SaslException: GSS initiate failed [Caused by GSSException: No valid credentials provided (Mechanism level: Failed to find any Kerberos tgt)]
mkdir: Failed on local exception: java.io.IOException: javax.security.sasl.SaslException: GSS initiate failed [Caused by GSSException: No valid credentials provided (Mechanism level: Failed to find any Kerberos tgt)]; Host Details : local host is: "slaver2/192.168.1.169"; destination host is: "master":4007; 
[ccc@slaver2 hadoop]$ hdfs dfs -ls /
21/10/26 07:27:13 WARN ipc.Client: Exception encountered while connecting to the server : javax.security.sasl.SaslException: GSS initiate failed [Caused by GSSException: No valid credentials provided (Mechanism level: Failed to find any Kerberos tgt)]
ls: Failed on local exception: java.io.IOException: javax.security.sasl.SaslException: GSS initiate failed [Caused by GSSException: No valid credentials provided (Mechanism level: Failed to find any Kerberos tgt)]; Host Details : local host is: "slaver2/192.168.1.169"; destination host is: "master":4007; 
```



#### hive kerberos配置

```
<configuration>

    <property>
        <name>hive.exec.local.scratchdir</name>
        <value>/data/hive-2.3.5/tmp</value>
    </property>

    <property>
        <name>hive.hwi.listen.host</name>
        <value>0.0.0.0</value>
    </property>

    <property>
        <name>hive.hwi.listen.port</name>
        <value>7002</value>
    </property>

    <property>
        <name>hive.llap.daemon.output.service.port</name>
        <value>7009</value>
    </property>

    <property>
        <name>hive.llap.daemon.rpc.port</name>
        <value>7007</value>
    </property>

    <property>
        <name>hive.llap.daemon.web.port</name>
        <value>7008</value>
    </property>

    <property>
        <name>hive.llap.daemon.yarn.shuffle.port</name>
        <value>7006</value>
    </property>

    <property>
        <name>hive.llap.management.rpc.port</name>
        <value>7005</value>
    </property>

    <property>
        <name>hive.metastore.db.encoding</name>
        <value>UTF-8</value>
    </property>

    <property>
        <name>hive.metastore.metrics.enabled</name>
        <value>false</value>
    </property>

    <property>
        <name>hive.metastore.port</name>
        <value>7004</value>
    </property>

    <property>
        <name>hive.metastore.schema.verification</name>
        <value>false</value>
    </property>

    <property>
        <name>hive.metastore.schema.verification.record.version</name>
        <value>false</value>
    </property>

    <property>
        <name>hive.metastore.warehouse.dir</name>
        <value>/usr/hive/warehouse</value>
    </property>

    <property>
        <name>hive.querylog.location</name>
        <value>/data/hive-2.3.5/tmp</value>
    </property>

    <property>
        <name>hive.security.authorization.sqlstd.confwhitelist.append</name>
        <value>hive.input.format</value>
    </property>

    <property>
        <name>hive.server2.logging.operation.log.location</name>
        <value>/data/hive-2.3.5/tmpoperation_logs</value>
    </property>

    <property>
        <name>hive.server2.metrics.enabled</name>
        <value>true</value>
    </property>

    <property>
        <name>hive.server2.support.dynamic.service.discovery</name>
        <value>false</value>
    </property>

    <property>
        <name>hive.server2.thrift.bind.host</name>
        <value>192.168.1.167</value>
    </property>

    <property>
        <name>hive.server2.thrift.http.port</name>
        <value>7000</value>
    </property>

    <property>
        <name>hive.server2.thrift.port</name>
        <value>7001</value>
    </property>

    <property>
        <name>hive.server2.webui.host</name>
        <value>0.0.0.0</value>
    </property>

    <property>
        <name>hive.server2.webui.port</name>
        <value>7003</value>
    </property>

<!--  
    <property>
        <name>io.compression.codec.lzo.class</name>
        <value>com.hadoop.compression.lzo.LzoCodec</value>
    </property>

    <property>
        <name>io.compression.codecs</name>
        <value>org.apache.hadoop.io.compress.DefaultCodec,org.apache.hadoop.io.compress.GzipCodec,com.hadoop.compression.lzo.LzoCodec,com.hadoop.compression.lzo.LzopCodec,org.apache.hadoop.io.compress.SnappyCodec</value>
    </property>
-->
    <property>
        <name>javax.jdo.option.ConnectionDriverName</name>
        <value>com.mysql.jdbc.Driver</value>
    </property>

    <property>
        <name>javax.jdo.option.ConnectionPassword</name>
        <value>root</value>
    </property>

    <property>
        <name>javax.jdo.option.ConnectionURL</name>
        <value>jdbc:mysql://localhost:3306/hivemetastore?useSSL=false</value>
    </property>

    <property>
        <name>javax.jdo.option.ConnectionUserName</name>
        <value>root</value>
    </property>
    <property>
        <name>hive.downloaded.resources.dir</name>
        <value>/data/hive-2.3.5/tmp/${hive.session.id}_resources</value>
    </property>

    <property>
        <name>hive.exec.local.scratchdir</name>
        <value>/data/hive-2.3.5/tmp</value>
    </property>
<property>
       <name>hive.server2.enable.doAs</name>
       <value>true</value>
 </property>

 <property>
       <name>hive.server2.authentication</name>
       <value>KERBEROS</value>
 </property>
 <property>
       <name>hive.server2.authentication.kerberos.principal</name>
       <value>hadoop/master@HADOOP.COM</value>
 </property>
 <property>
       <name>hive.server2.authentication.kerberos.keytab</name>
       <value>/data/hadoop.keytab</value>
 </property>
 <property>
       <name>hive.server2.authentication.spnego.keytab</name>
       <value>/data/hadoop.keytab</value>
 </property>
 <property>
       <name>hive.server2.authentication.spnego.principal</name>
       <value>hadoop/master@HADOOP.COM</value>
 </property>
 <property>
       <name>hive.metastore.sasl.enabled</name>
       <value>true</value>
 </property>
 <property>
       <name>hive.metastore.kerberos.keytab.file</name>
       <value>/data/hadoop.keytab</value>
 </property>
 <property>
       <name>hive.metastore.kerberos.principal</name>
       <value>hadoop/master@HADOOP.COM</value>
 </property>

</configuration>
```

#### hive kerberos 测试

已注册用户启动hive cli

```
[hadoop@master ~]$ hive
which: no hbase in (/usr/local/bin:/usr/bin:/usr/local/sbin:/usr/sbin:/data/jdk1.8.0_212/bin:/data/hadoop-2.8.5/bin:/data/hadoop-2.8.5/sbin:/data/maven-3.8.3/bin:/data/spark-3.1.2/bin:/data/hive-2.3.5/bin:/home/hadoop/.local/bin:/home/hadoop/bin)
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/data/hive-2.3.5/lib/log4j-slf4j-impl-2.6.2.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/data/hadoop-2.8.5/share/hadoop/common/lib/slf4j-log4j12-1.7.10.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [org.apache.logging.slf4j.Log4jLoggerFactory]

Logging initialized using configuration in file:/data/hive-2.3.5/conf/hive-log4j2.properties Async: true
Hive-on-MR is deprecated in Hive 2 and may not be available in the future versions. Consider using a different execution engine (i.e. spark, tez) or using Hive 1.X releases.
hive> show databases;
OK
default
test
test01
test_qa
Time taken: 5.186 seconds, Fetched: 4 row(s)
```

无权限用户

```
Caused by: java.io.IOException: javax.security.sasl.SaslException: GSS initiate failed [Caused by GSSException: No valid credentials provided (Mechanism level: Failed to find any Kerberos tgt)]
        at org.apache.hadoop.ipc.Client$Connection$1.run(Client.java:755)
        at java.security.AccessController.doPrivileged(Native Method)
        at javax.security.auth.Subject.doAs(Subject.java:422)
        at org.apache.hadoop.security.UserGroupInformation.doAs(UserGroupInformation.java:1844)
        at org.apache.hadoop.ipc.Client$Connection.handleSaslConnectionFailure(Client.java:718)
        at org.apache.hadoop.ipc.Client$Connection.setupIOstreams(Client.java:811)
        at org.apache.hadoop.ipc.Client$Connection.access$3500(Client.java:410)
        at org.apache.hadoop.ipc.Client.getConnection(Client.java:1550)
        at org.apache.hadoop.ipc.Client.call(Client.java:1381)
        ... 33 more
Caused by: javax.security.sasl.SaslException: GSS initiate failed [Caused by GSSException: No valid credentials provided (Mechanism level: Failed to find any Kerberos tgt)]
        at com.sun.security.sasl.gsskerb.GssKrb5Client.evaluateChallenge(GssKrb5Client.java:211)
        at org.apache.hadoop.security.SaslRpcClient.saslConnect(SaslRpcClient.java:406)
        at org.apache.hadoop.ipc.Client$Connection.setupSaslConnection(Client.java:614)
        at org.apache.hadoop.ipc.Client$Connection.access$2200(Client.java:410)
        at org.apache.hadoop.ipc.Client$Connection$2.run(Client.java:798)
        at org.apache.hadoop.ipc.Client$Connection$2.run(Client.java:794)
        at java.security.AccessController.doPrivileged(Native Method)
        at javax.security.auth.Subject.doAs(Subject.java:422)
        at org.apache.hadoop.security.UserGroupInformation.doAs(UserGroupInformation.java:1844)
        at org.apache.hadoop.ipc.Client$Connection.setupIOstreams(Client.java:793)
        ... 36 more
Caused by: GSSException: No valid credentials provided (Mechanism level: Failed to find any Kerberos tgt)
        at sun.security.jgss.krb5.Krb5InitCredential.getInstance(Krb5InitCredential.java:147)
        at sun.security.jgss.krb5.Krb5MechFactory.getCredentialElement(Krb5MechFactory.java:122)
        at sun.security.jgss.krb5.Krb5MechFactory.getMechanismContext(Krb5MechFactory.java:187)
        at sun.security.jgss.GSSManagerImpl.getMechanismContext(GSSManagerImpl.java:224)
        at sun.security.jgss.GSSContextImpl.initSecContext(GSSContextImpl.java:212)
        at sun.security.jgss.GSSContextImpl.initSecContext(GSSContextImpl.java:179)
        at com.sun.security.sasl.gsskerb.GssKrb5Client.evaluateChallenge(GssKrb5Client.java:192)
        ... 45 more
[bbb@master hadoop]$
```

hiveserver2启动kerberos认证后无法通过默认jdbc url连接

```
[hadoop@master ~]$ /data/hive-2.3.5/bin/beeline -u "jdbc:hive2://localhost:7001/default"
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/data/hive-2.3.5/lib/log4j-slf4j-impl-2.6.2.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/data/hadoop-2.8.5/share/hadoop/common/lib/slf4j-log4j12-1.7.10.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [org.apache.logging.slf4j.Log4jLoggerFactory]
Connecting to jdbc:hive2://localhost:7001/default
21/10/26 07:34:40 [main]: WARN jdbc.HiveConnection: Failed to connect to localhost:7001
Unknown HS2 problem when communicating with Thrift server.
Error: Could not open client transport with JDBC Uri: jdbc:hive2://localhost:7001/default: Peer indicated failure: Unsupported mechanism type PLAIN (state=08S01,code=0)
Beeline version 2.3.5 by Apache Hive
```

需要增加参数，其中principal=的这个主体是hiveserver2配置文件配置的principal。是固定的。与启动连接hiveserver2用户无关

```
[hadoop@master ~]$ /data/hive-2.3.5/bin/beeline -u "jdbc:hive2://localhost:7001/default;principal=hadoop/master@HADOOP.COM" -n hadoop
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/data/hive-2.3.5/lib/log4j-slf4j-impl-2.6.2.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/data/hadoop-2.8.5/share/hadoop/common/lib/slf4j-log4j12-1.7.10.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [org.apache.logging.slf4j.Log4jLoggerFactory]
Connecting to jdbc:hive2://localhost:7001/default;principal=hadoop/master@HADOOP.COM
Connected to: Apache Hive (version 2.3.5)
Driver: Hive JDBC (version 2.3.5)
Transaction isolation: TRANSACTION_REPEATABLE_READ
Beeline version 2.3.5 by Apache Hive
0: jdbc:hive2://localhost:7001/default> 
```

windows 使用dbeaver工具连接hive正常方式也无法连接，需修改hive 通用驱动设置，点击编辑驱动设置，修改jdbc url

```
默认
jdbc:hive2://{host}[:{port}][/{database}]
修改后
jdbc:hive2://{host}[:{port}][/{database}];AuthMech=1;KrbRealm=HADOOP.COM;KrbHostFQDN=master;KrbServiceName=hadoop;KrbAuthType=2
KrbServiceName是hiveserver2配置的主体名包含的用户名
KrbHostFQDNhiveserver2配置的主体包含的主机名
KrbRealm主体包含的域名
```

同时驱动需要替换

参考

https://www.cnblogs.com/fivedays/p/12808488.html

https://blog.csdn.net/guoqing2017/article/details/113696196

驱动下载地址https://link.csdn.net/?target=https%3A%2F%2Fdownloads.cloudera.com%2Fconnectors%2Fhive_jdbc_2.6.5.1007.zip

在编辑驱动页面->库->删除默认库再添加下载的驱动jar包->点击找到类->选择com.cloudera.hive.jdbc.HS2Driver

#### spark kerberos

spark 需将hdfs-site.xml  hive-site.xml复制到spark conf路径下

spark-submit同spark-sql

spark-sql启动 无权限用户启动sqark-sql报错

```
Caused by: java.io.IOException: org.apache.hadoop.security.AccessControlException: Client cannot authenticate via:[TOKEN, KERBEROS]
        at org.apache.hadoop.ipc.Client$Connection$1.run(Client.java:760)
        at java.security.AccessController.doPrivileged(Native Method)
        at javax.security.auth.Subject.doAs(Subject.java:422)
        at org.apache.hadoop.security.UserGroupInformation.doAs(UserGroupInformation.java:1730)
        at org.apache.hadoop.ipc.Client$Connection.handleSaslConnectionFailure(Client.java:723)
        at org.apache.hadoop.ipc.Client$Connection.setupIOstreams(Client.java:817)
        at org.apache.hadoop.ipc.Client$Connection.access$3700(Client.java:411)
        at org.apache.hadoop.ipc.Client.getConnection(Client.java:1572)
        at org.apache.hadoop.ipc.Client.call(Client.java:1403)
        ... 39 more
Caused by: org.apache.hadoop.security.AccessControlException: Client cannot authenticate via:[TOKEN, KERBEROS]
        at org.apache.hadoop.security.SaslRpcClient.selectSaslClient(SaslRpcClient.java:173)
        at org.apache.hadoop.security.SaslRpcClient.saslConnect(SaslRpcClient.java:390)
        at org.apache.hadoop.ipc.Client$Connection.setupSaslConnection(Client.java:617)
        at org.apache.hadoop.ipc.Client$Connection.access$2300(Client.java:411)
        at org.apache.hadoop.ipc.Client$Connection$2.run(Client.java:804)
        at org.apache.hadoop.ipc.Client$Connection$2.run(Client.java:800)
        at java.security.AccessController.doPrivileged(Native Method)
        at javax.security.auth.Subject.doAs(Subject.java:422)
        at org.apache.hadoop.security.UserGroupInformation.doAs(UserGroupInformation.java:1730)
        at org.apache.hadoop.ipc.Client$Connection.setupIOstreams(Client.java:800)
        ... 42 more
[bbb@master home]$ exit
```

使用他人密钥登录

```
[bbb@master hadoop]$ spark-sql --keytab /data/hadoop.keytab --principal hadoop/master@HADOOP.COM
Caused by: javax.security.auth.login.LoginException: Unable to obtain password from user

        at com.sun.security.auth.module.Krb5LoginModule.promptForPass(Krb5LoginModule.java:897)
        at com.sun.security.auth.module.Krb5LoginModule.attemptAuthentication(Krb5LoginModule.java:760)
        at com.sun.security.auth.module.Krb5LoginModule.login(Krb5LoginModule.java:617)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:498)
        at javax.security.auth.login.LoginContext.invoke(LoginContext.java:755)
        at javax.security.auth.login.LoginContext.access$000(LoginContext.java:195)
        at javax.security.auth.login.LoginContext$4.run(LoginContext.java:682)
        at javax.security.auth.login.LoginContext$4.run(LoginContext.java:680)
        at java.security.AccessController.doPrivileged(Native Method)
        at javax.security.auth.login.LoginContext.invokePriv(LoginContext.java:680)
        at javax.security.auth.login.LoginContext.login(LoginContext.java:587)
        at org.apache.hadoop.security.UserGroupInformation$HadoopLoginContext.login(UserGroupInformation.java:1926)
        at org.apache.hadoop.security.UserGroupInformation.doSubjectLogin(UserGroupInformation.java:1837)
        ... 10 more
[bbb@master hadoop]$ 
```

正常已注册用户，且对秘钥文件有读写权限

```
[aaa@master hadoop]$ spark-sql --keytab /data/hadoop.keytab --principal hadoop/master@HADOOP.COM
21/10/26 08:04:57 WARN HiveConf: HiveConf of name hive.metastore.db.encoding does not exist
21/10/26 08:04:57 WARN HiveConf: HiveConf of name hive.hwi.listen.host does not exist
21/10/26 08:04:57 WARN HiveConf: HiveConf of name hive.hwi.listen.port does not exist
21/10/26 08:04:57 WARN HiveConf: HiveConf of name hive.metastore.db.encoding does not exist
21/10/26 08:04:57 WARN HiveConf: HiveConf of name hive.hwi.listen.host does not exist
21/10/26 08:04:57 WARN HiveConf: HiveConf of name hive.hwi.listen.port does not exist
21/10/26 08:05:01 WARN HiveConf: HiveConf of name hive.metastore.db.encoding does not exist
21/10/26 08:05:01 WARN HiveConf: HiveConf of name hive.stats.jdbc.timeout does not exist
21/10/26 08:05:01 WARN HiveConf: HiveConf of name hive.hwi.listen.host does not exist
21/10/26 08:05:01 WARN HiveConf: HiveConf of name hive.hwi.listen.port does not exist
21/10/26 08:05:01 WARN HiveConf: HiveConf of name hive.stats.retries.wait does not exist
21/10/26 08:05:01 WARN HiveConf: HiveConf of name hive.metastore.db.encoding does not exist
21/10/26 08:05:01 WARN HiveConf: HiveConf of name hive.hwi.listen.host does not exist
21/10/26 08:05:01 WARN HiveConf: HiveConf of name hive.hwi.listen.port does not exist
Spark master: local[*], Application Id: local-1635206699139
spark-sql> 
```

如果用户已经注册在秘钥文件里，但是对秘钥文件没有读写权限，会报错，需设置密钥文件对该用户有读写权限

```
[aaa@master hadoop]$ spark-sql --keytab /data/hadoop.keytab --principal hadoop/master@HADOOP.COM
Exception in thread "main" org.apache.hadoop.security.KerberosAuthException: failure to login: for principal: hadoop/master@HADOOP.COM from keytab /data/hadoop.keytab javax.security.auth.login.LoginException: Unable to obtain password from user

        at org.apache.hadoop.security.UserGroupInformation.doSubjectLogin(UserGroupInformation.java:1847)
        at org.apache.hadoop.security.UserGroupInformation.loginUserFromKeytabAndReturnUGI(UserGroupInformation.java:1215)
        at org.apache.hadoop.security.UserGroupInformation.loginUserFromKeytab(UserGroupInformation.java:1008)
        at org.apache.spark.deploy.SparkSubmit.prepareSubmitEnvironment(SparkSubmit.scala:358)
        at org.apache.spark.deploy.SparkSubmit.org$apache$spark$deploy$SparkSubmit$$runMain(SparkSubmit.scala:894)
        at org.apache.spark.deploy.SparkSubmit.doRunMain$1(SparkSubmit.scala:180)
        at org.apache.spark.deploy.SparkSubmit.submit(SparkSubmit.scala:203)
        at org.apache.spark.deploy.SparkSubmit.doSubmit(SparkSubmit.scala:90)
        at org.apache.spark.deploy.SparkSubmit$$anon$2.doSubmit(SparkSubmit.scala:1039)
        at org.apache.spark.deploy.SparkSubmit$.main(SparkSubmit.scala:1048)
        at org.apache.spark.deploy.SparkSubmit.main(SparkSubmit.scala)
Caused by: javax.security.auth.login.LoginException: Unable to obtain password from user

        at com.sun.security.auth.module.Krb5LoginModule.promptForPass(Krb5LoginModule.java:897)
        at com.sun.security.auth.module.Krb5LoginModule.attemptAuthentication(Krb5LoginModule.java:760)
        at com.sun.security.auth.module.Krb5LoginModule.login(Krb5LoginModule.java:617)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:498)
        at javax.security.auth.login.LoginContext.invoke(LoginContext.java:755)
        at javax.security.auth.login.LoginContext.access$000(LoginContext.java:195)
        at javax.security.auth.login.LoginContext$4.run(LoginContext.java:682)
        at javax.security.auth.login.LoginContext$4.run(LoginContext.java:680)
        at java.security.AccessController.doPrivileged(Native Method)
        at javax.security.auth.login.LoginContext.invokePriv(LoginContext.java:680)
        at javax.security.auth.login.LoginContext.login(LoginContext.java:587)
        at org.apache.hadoop.security.UserGroupInformation$HadoopLoginContext.login(UserGroupInformation.java:1926)
        at org.apache.hadoop.security.UserGroupInformation.doSubjectLogin(UserGroupInformation.java:1837)
        ... 10 more
[aaa@master hadoop]$ 
```

#### 启用kerberos对现有calc中台的影响

##### kerberos现有测试问题

1.启用kerberos后修改生成ticket生效时间，创建新主体，修改主体ticket生效时间，klist查看还是默认时间

解决方案：

https://www.aboutyun.com/home.php?mod=space&uid=1407&do=blog&quickforward=1&id=3090

https://www.cnblogs.com/gentlemanhai/p/10824514.html

验证max_life 由以下5个值的最小值决定 ，测试结果：可以修改ticket生效时间

```
1.kerberos Server上的/var/kerberos/krb5kdbc/kdc.conf中的max_life
2.内置principal krbtgt的maxmum ticket life,可在kadmin命令下执行getprinc命令查看
3.Principal的maximum tiket life time，在kadmin命令下用getprinc命令查看
4.kerberos client上/etc/krb5.conf的ticket_lifetime
5.kinit –l 参数后面指定的时间
```

```
Usage: kinit [-V] [-l lifetime] [-s start_time] 
        [-r renewable_life] [-f | -F | --forwardable | --noforwardable] 
        [-p | -P | --proxiable | --noproxiable] 
        -n [-a | -A | --addresses | --noaddresses] 
        [--request-pac | --no-request-pac] 
        [-C | --canonicalize] 
        [-E | --enterprise] 
        [-v] [-R] [-k [-i|-t keytab_file]] [-c cachename] 
        [-S service_name] [-T ticket_armor_cache]
        [-X <attribute>[=<value>]] [principal]

    options:
        -V verbose
        -l lifetime
        -s start time
        -r renewable lifetime
        -f forwardable
        -F not forwardable
        -p proxiable
        -P not proxiable
        -n anonymous
        -a include addresses
        -A do not include addresses
        -v validate
        -R renew
        -C canonicalize
        -E client is enterprise principal name
        -k use keytab
        -i use default client keytab (with -k)
        -t filename of keytab to use
        -c Kerberos 5 cache name
        -S service
        -T armor credential cache
        -X <attribute>[=<value>]
```

手动修改ticket生命周期

```
modprinc -maxrenewlife 90days +allow_renewable hadoop/master@HADOOP.COM
modprinc -maxlife 90days +allow_renewable hadoop/master@HADOOP.COM
```



2.namenode web 无法查看文件目录,权限验证失败

Authentication failed when trying to open /webhdfs/v1/?op=LISTSTATUS: Unauthorized.

解决方案：

https://www.cloudera.com/documentation/enterprise/5-5-x/topics/cdh_sg_browser_access_kerberos_protected_url.html

https://blog.csdn.net/qq_40341628/article/details/84991443?utm_medium=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7Edefault-14.no_search_link&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2%7Edefault%7ECTRLIST%7Edefault-14.no_search_link

##### jobserver 提交spark job验证kerberos

job json

```
{
  "job_type": "sql",
  "job_name": "test002",
  "params": {
  },
  "hive_meta_uri": "thrift://192.168.1.167:7004",
  "routes": [
    {
      "before": "1001",
      "after": "1002"
    }
  ],
  "tasks": {
    "1001": {
      "url": "jdbc:mysql://xx.xx.xxx.xx:3306/test",
      "user": "root",
      "password": "Kq0FYUSc42",
      "table": "ods_bugsdaily",
      "tempView": "ods_bugsdaily_tmp",
      "columns": [
      ],
      "sql": "select *,DATE_FORMAT(modified,'%Y%m%d') pady from ods_bugsdaily",
      "name": "mysql_reader",
      "strategy": "mysql.reader"
    },
    "1002": {
      "name": "hive_writer",
      "strategy": "hive.sql",
      "sql": "insert overwrite table test_qa.ods_bugs_inc_daily_test02  partition(pday) select * from ods_bugsdaily_tmp "
    }
  }
}
```

先不配置kerberos参数

```
{
  "params": [

  ],
  "config": {
    "hive.exec.dynamic.partition": true,
    "hive.exec.dynamic.partition.mode": "nonstrict",
    "mapreduce.map.memory.mb": 15000,
    "mapreduce.reduce.memory.mb": 15000,
    "hive.merge.mapredfiles": true,
    "hive.exec.max.created.files": 100000,
    "hive.exec.max.dynamic.partitions": 100000,
    "hive.exec.max.dynamic.partitions.pernode": 100000
  }
}
```

直接注册，

```
register_conf.py -f C:\TencentProject\code\calc\dist\src\calc\test\k8s\hivesql_8.json -n test -g 0 -p {\"params\":[],\"config\":{\"hive.exec.dynamic.partition\":true,\"hive.exec.dynamic.partition.mode\":\"nonstrict\",\"mapreduce.map.memory.mb\":15000,\"mapreduce.reduce.memory.mb\":15000,\"hive.merge.mapredfiles\":true,\"hive.exec.max.created.files\":100000,\"hive.exec.max.dynamic.partitions\":100000,\"hive.exec.max.dynamic.partitions.pernode\":100000}}
```

在开启了kerberos的集群上提交 运行成功 而在windows idea本地提交相同作业报错

```
[INFO ] 	 2021-10-26 15:01:10.577 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 Caused by: org.apache.hadoop.ipc.RemoteException(org.apache.hadoop.security.AccessControlException): SIMPLE authentication is not enabled.  Available:[TOKEN, KERBEROS] 
[INFO ] 	 2021-10-26 15:01:10.577 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 	at org.apache.hadoop.ipc.Client.getRpcResponse(Client.java:1511) 
[INFO ] 	 2021-10-26 15:01:10.577 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 	at org.apache.hadoop.ipc.Client.call(Client.java:1457) 
[INFO ] 	 2021-10-26 15:01:10.577 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 	at org.apache.hadoop.ipc.Client.call(Client.java:1367) 
[INFO ] 	 2021-10-26 15:01:10.577 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 	at org.apache.hadoop.ipc.ProtobufRpcEngine$Invoker.invoke(ProtobufRpcEngine.java:228) 
[INFO ] 	 2021-10-26 15:01:10.577 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 	at org.apache.hadoop.ipc.ProtobufRpcEngine$Invoker.invoke(ProtobufRpcEngine.java:116) 
[INFO ] 	 2021-10-26 15:01:10.577 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 	at com.sun.proxy.$Proxy16.getFileInfo(Unknown Source) 
[INFO ] 	 2021-10-26 15:01:10.577 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 	at org.apache.hadoop.hdfs.protocolPB.ClientNamenodeProtocolTranslatorPB.getFileInfo(ClientNamenodeProtocolTranslatorPB.java:903) 
[INFO ] 	 2021-10-26 15:01:10.577 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) 
[INFO ] 	 2021-10-26 15:01:10.577 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) 
[INFO ] 	 2021-10-26 15:01:10.577 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) 
[INFO ] 	 2021-10-26 15:01:10.578 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 	at java.lang.reflect.Method.invoke(Method.java:498) 
[INFO ] 	 2021-10-26 15:01:10.578 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 	at org.apache.hadoop.io.retry.RetryInvocationHandler.invokeMethod(RetryInvocationHandler.java:422) 
[INFO ] 	 2021-10-26 15:01:10.578 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 	at org.apache.hadoop.io.retry.RetryInvocationHandler$Call.invokeMethod(RetryInvocationHandler.java:165) 
[INFO ] 	 2021-10-26 15:01:10.578 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 	at org.apache.hadoop.io.retry.RetryInvocationHandler$Call.invoke(RetryInvocationHandler.java:157) 
[INFO ] 	 2021-10-26 15:01:10.578 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 	at org.apache.hadoop.io.retry.RetryInvocationHandler$Call.invokeOnce(RetryInvocationHandler.java:95) 
[INFO ] 	 2021-10-26 15:01:10.578 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 	at org.apache.hadoop.io.retry.RetryInvocationHandler.invoke(RetryInvocationHandler.java:359) 
[INFO ] 	 2021-10-26 15:01:10.578 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 	at com.sun.proxy.$Proxy17.getFileInfo(Unknown Source) 
[INFO ] 	 2021-10-26 15:01:10.578 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 	at org.apache.hadoop.hdfs.DFSClient.getFileInfo(DFSClient.java:1665) 
[INFO ] 	 2021-10-26 15:01:10.578 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 	... 93 more 
[INFO ] 	 2021-10-26 15:01:10.578 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 21/10/26 15:01:10 ERROR Main: java.lang.RuntimeException: org.apache.hadoop.security.AccessControlException: SIMPLE authentication is not enabled.  Available:[TOKEN, KERBEROS] 
```

对比发现，服务器上启动jobserver的用户是已经通过kdc client成功获取票据的用户,其他用户通过jobserver提交spark-sql作业不需要再次验证kerberos权限，提交的作业中包不包含spark.kerberos.principal和spark.kerberos.keytab都能正常运行

idea本地用户测试时，运行jvm用户是本地计算机名，没有配置kerberos权限。spark.launch.setconf 读取的集群配置文件是增加了kerberos。而windows本地设置的HADOOP_HOME是未增加kerberos开发集群的配置。从结果上看优先找的HADOOP_HOME，其中core—site.xml 无此项设置，默认SIMPLE

```
<property>
  <name>hadoop.security.authentication</name>
  <value>kerberos</value>
</property>
```

增加环境变量HADOOP_CONF_DIR到kerberos 集群hadoop配置文件，再次测试

```
[INFO ] 	 2021-10-27 00:48:17.658 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 21/10/27 00:48:17 WARN Client: Exception encountered while connecting to the server : org.apache.hadoop.security.AccessControlException: Client cannot authenticate via:[TOKEN, KERBEROS] 
[INFO ] 	 2021-10-27 00:48:17.668 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 21/10/27 00:48:17 ERROR SparkContext: Error initializing SparkContext. 
[INFO ] 	 2021-10-27 00:48:17.668 	 org.apache.spark.launcher.app.test 	 redirect 	 63 	 [launcher-proc-1] 	 java.io.IOException: DestHost:destPort master:4007 , LocalHost:localPort V-ANAXXION-NBQP/192.168.255.10:0. Failed on local exception: java.io.IOException: org.apache.hadoop.security.AccessControlException: Client cannot authenticate via:[TOKEN, KERBEROS] 
```

对比发现datax没有这个问题。查看datax源码，发现datax hdfs插件使用了UserGroupInformation.loginUserFromKeytab(kerberosPrincipal, kerberosKeytabFilePath)。参考datax代码，在spark run中使用UserGroupInformation登录，使用没有kerberos权限的用户ccc启动jobserver 测试发现spark run hadoop鉴权成功

spark-sql需要参数

spark.kerberos.principal： 已注册主体名称

spark.kerberos.keytab：keytab文件路径

例如：

```
{
  "params": [

  ],
  "config": {
    "spark.kerberos.principal": "hadoop/master@HADOOP.COM", 
    "spark.kerberos.keytab": "/data/hadoop.keytab",
    "hive.exec.dynamic.partition": true,
    "hive.exec.dynamic.partition.mode": "nonstrict",
    "mapreduce.map.memory.mb": 15000,
    "mapreduce.reduce.memory.mb": 15000,
    "hive.merge.mapredfiles": true,
    "hive.exec.max.created.files": 100000,
    "hive.exec.max.dynamic.partitions": 100000,
    "hive.exec.max.dynamic.partitions.pernode": 100000
  }
}
```

注册作业

```
python3 register_conf.py -f C:\TencentProject\code\calc\dist\src\calc\test\k8s\hivesql_8.json -n test -g 0 -p {\"params\":[],\"config\":{\"spark.kerberos.principal\":\"hadoop/master@HADOOP.COM\",\"spark.kerberos.keytab\":\"/data/hadoop.keytab\",\"hive.exec.dynamic.partition\":true,\"hive.exec.dynamic.partition.mode\":\"nonstrict\",\"mapreduce.map.memory.mb\":15000,\"mapreduce.reduce.memory.mb\":15000,\"hive.merge.mapredfiles\":true,\"hive.exec.max.created.files\":100000,\"hive.exec.max.dynamic.partitions\":100000,\"hive.exec.max.dynamic.partitions.pernode\":100000}}
```

提交作业

```
[ccc@master bin]$  python3 run_job.py 492
作业提交
server url: http://192.168.1.167:8001/calc/
作业状态为: 0
作业状态为: 0
作业状态为: 0
作业状态为: 0
作业状态为: 0
作业状态为: 0
作业状态为: 0
作业状态为: 0
作业状态为: 0
作业状态为: 0
作业状态为: 1
作业状态为: 1
作业状态为: 1
作业状态为: 1
作业状态为: 1
作业状态为: 1
作业状态为: 2
作业状态为: 2
作业状态为: 2
作业状态为: 2
作业状态为: 2
作业状态为: 3
[ccc@master bin]$ 
```

##### DataX作业测试hdfs相关插件验证kerberos

datax官方原版hdfs reader和hdfs writer作业配置预留有支持kerberos参数，hdfs writer插件新增自动建表和自动导数据功能是基于hive jdbc连接，经过测试可在jdbc url直接增加参数principal

```
 "haveKerberos" : "true",
 "kerberosKeytabFilePath": "C:\\mytemp\\hadoop.keytab",
 "kerberosPrincipal": "hadoop/master@HADOOP.COM"
```

```
jdbc:hive2://192.168.1.167:7001/;principal=hadoop/master@HADOOP.COM
```

json作业配置

```
{
  "job": {
    "content": [
      {
        "reader": {
          "name": "httpreader",
          "parameter": {
            "url": "http://xx.xx.xxx.xx:8001/calc/jobHistory/stateStore",
            "method": "post",
            "line_key": "data_json",
            "headers": {
              "Content-type": "application/json"
            },
            "params": {
            },
            "schema": {
              "success_key": "$.message",
              "success_value": "SUCCESS",
              "line_path" : "$",
              "columns": [
                {
                  "name": "code",
                  "type": "string"
                },
                {
                  "name": "message",
                  "type": "string"
                },
                {
                  "name": "data",
                  "type": "string"
                }
              ]
            },
            "request_bodies": [
              {
                "jobId": 432,
                "runTheDate": "20210601",
                "jobState": 3
              },
              {
                "jobId": 432,
                "runTheDate": "20210601",
                "jobState": 3
              },
              {
                "jobId": 432,
                "runTheDate": "20210601",
                "jobState": 3
              }
            ]
          }
        },
        "writer": {
          "name": "hdfswriter",
          "parameter": {
            "column": [
              {
                "name": "code",
                "type": "string"
              },
              {
                "name": "message",
                "type": "string"
              },
              {
                "name": "data",
                "type": "string"
              },
              {
                "name": "data_json",
                "type": "string"
              }
            ],
            "defaultFS": "hdfs://192.168.1.167:4007",
            "jdbcUrl": "jdbc:hive2://192.168.1.167:7001/;principal=hadoop/master@HADOOP.COM",
            "username": "hadoop",
            "password": "",
            "database": "test01",
            "table": "aaa",
            "tablePath": "/usr/hive/warehouse/test01/aaa",
            "external": "false",
            "fieldDelimiter": "\t",
            "fileName": "aaa",
            "fileType": "text",
            "path": "/usr/hive/warehouse/test01/aaa",
            "writeMode": "append",
            "haveKerberos" : "true",
            "kerberosKeytabFilePath": "C:\\mytemp\\hadoop.keytab",
            "kerberosPrincipal": "hadoop/master@HADOOP.COM"
          }
        }
      }
    ],
    "setting": {
      "speed": {
        "channel": 1
      }
    }
  }
}

```

##### datax测试结果

不加Kerberos参数，windows 删除已验证票据，直接使用datax.py执行作业。报错

```
SIMPLE authentication is not enabled.  Available:[TOKEN, KERBEROS]
```

不加Kerberos参数，windows 使用密码 初始化账户xiongan@HADOOP.COM

```
C:\Users\xiongan>klist
Credentials cache C:\Users\xiongan\krb5cc_xiongan not found.
C:\Users\xiongan>cd C:\Program Files\MIT\Kerberos\bin
C:\Program Files\MIT\Kerberos\bin>kinit xiongan
Password for xiongan@HADOOP.COM:
C:\Program Files\MIT\Kerberos\bin>klist
Ticket cache: API:Initial default ccache
Default principal: xiongan@HADOOP.COM
Valid starting     Expires            Service principal
10/26/21 21:36:57  10/27/21 21:36:57  krbtgt/HADOOP.COM@HADOOP.COM
```

同样报错

```
SIMPLE authentication is not enabled.  Available:[TOKEN, KERBEROS]
```

查看datax代码，dataxhdfs插件通过UserGroupInformation.loginUserFromKeytab(kerberosPrincipal, kerberosKeytabFilePath)实现了Kerberos认证

增加Kerberos参数 ，windows 删除已验证票据，使用秘钥验证，运行成功

```
"haveKerberos" : "true",
"kerberosKeytabFilePath": "C:\\mytemp\\hadoop.keytab",
"kerberosPrincipal": "hadoop/master@HADOOP.COM"
```

增加Kerberos参数，秘钥使用hadoop.keytab，主体指定hadoop/master@HADOOP.COM，windows 使用密码 初始化账户xiongan@HADOOP.COM。

运行成功，查看log是使用的秘钥验证

```
十月 26, 2021 9:39:51 下午 org.apache.hadoop.security.UserGroupInformation loginUserFromKeytab
信息: Login successful for user hadoop/master@HADOOP.COM using keytab file C:\mytemp\hadoop.keytab
```



##### 需解决问题：

1.kerberos现有测试问题：keytab失效时间修改和namenode web页面查看hdfs文件无权限，按网上方案没有成功，可能是配置问题

2.sdk脚本注册sql作业不能添加-p 参数，注册的sql作业无法做到kerberos认证

3.airflow已有自动采集任务需连接hiveserver2上传文件hdfs，需修改脚本支持kerberos

4.spark作业可以在注册作业时-conf添加参数配置，需要修改代码参考datax 增加hadoop认证到spark run中，本地测试hadoop认证在spark run中ok,根据测试结果，服务器上启动jobserver的用户应排除kerberos权限。需要注意的是启动jobserver的用户需要对keytab有读写权限 否则报错

```
 Caused by: javax.security.auth.login.LoginException: Unable to obtain password from user 
```

5.增加秘钥文件的信息表和关系表



## Ranger on kerberos验证

#### 安装ranger-admin

安装参考

https://blog.51cto.com/zero01/2550035

配置ranger admin 设置kerberos

mysql 的ranger使用用户需要增加权限

在mysql中输入以下命令

```
set global validate_password_length=6;
set global validate_password_policy=LOW;
create user 'ranger'@'%' identified by 'ranger';
grant all privileges on ranger.* to ranger@'%'  identified by 'ranger';
flush privileges;
```

配置kerberos参考

https://www.jianshu.com/p/3afd37f6fe00

http://blog.sina.com.cn/s/blog_167a8c6480102xqdc.html

修改ranger admin 的install.properties 文件其中的：

```
SQL_CONNECTOR_JAR=/data/hive-2.3.5/lib/mysql-connector-java-5.1.48.jar

db_root_user=root
db_root_password=root
db_host=localhost

db_name=ranger
db_user=ranger
db_password=ranger

rangerAdmin_password=ranger123
rangerTagsync_password=ranger123
rangerUsersync_password=ranger123
keyadmin_password=ranger123

#Source for Audit Store. Currently only solr is supported.
# * audit_store is solr
#audit_store=solr

policymgr_external_url=http://localhost:6080
policymgr_http_enabled=true
policymgr_https_keystore_file=
policymgr_https_keystore_keyalias=rangeradmin
policymgr_https_keystore_password=

# ------- UNIX User CONFIG ----------------
unix_user=root
unix_user_pwd=root
unix_group=root

#------------ Kerberos Config -----------------
spnego_principal=HTTP/master@HADOOP.COM
spnego_keytab=/data/http.keytab
token_valid=30
cookie_domain=
cookie_path=/
admin_principal=hadoop/master@HADOOP.COM
admin_keytab=/data/hadoop.keytab
lookup_principal=hadoop/master@HADOOP.COM
lookup_keytab=/data/hadoop.keytab
hadoop_conf=/data/hadoop-2.8.5/etc/hadoop
```

spnego_principal必须是HTTP为用户名,否则拉取不到策略

https://community.cloudera.com/t5/Support-Questions/Ranger-401-authentication-error-while-download-policy/td-p/299533

增加一个HTTP/master主体

```
kadmin.local:  addprinc HTTP/master
WARNING: no policy specified for HTTP/master@HADOOP.COM; defaulting to no policy
Enter password for principal "HTTP/master@HADOOP.COM": 
Re-enter password for principal "HTTP/master@HADOOP.COM": 
Principal "HTTP/master@HADOOP.COM" created.
```

```
kadmin.local:  xst -norandkey -k /data/http.keytab HTTP/master@HADOOP.COM
Entry for principal HTTP/master@HADOOP.COM with kvno 1, encryption type aes128-cts-hmac-sha1-96 added to keytab WRFILE:/data/http.keytab.
Entry for principal HTTP/master@HADOOP.COM with kvno 1, encryption type des3-cbc-sha1 added to keytab WRFILE:/data/http.keytab.
Entry for principal HTTP/master@HADOOP.COM with kvno 1, encryption type arcfour-hmac added to keytab WRFILE:/data/http.keytab.
Entry for principal HTTP/master@HADOOP.COM with kvno 1, encryption type camellia256-cts-cmac added to keytab WRFILE:/data/http.keytab.
Entry for principal HTTP/master@HADOOP.COM with kvno 1, encryption type camellia128-cts-cmac added to keytab WRFILE:/data/http.keytab.
Entry for principal HTTP/master@HADOOP.COM with kvno 1, encryption type des-hmac-sha1 added to keytab WRFILE:/data/http.keytab.
Entry for principal HTTP/master@HADOOP.COM with kvno 1, encryption type des-cbc-md5 added to keytab WRFILE:/data/http.keytab.
```

修改ranger-admin时区/data/ranger/admin/ews/ranger-admin-services.sh 

```
#if [[ ${JAVA_OPTS} != *"-Duser.timezone"* ]] ;then  export JAVA_OPTS=" ${JAVA_OPTS} -Duser.timezone=UTC" ;fi
if [[ ${JAVA_OPTS} != *"-Duser.timezone"* ]] ;then  export JAVA_OPTS=" ${JAVA_OPTS} -Duser.timezone=Asia/Shanghai" ;fi
```

执行/data/ranger/admin/setup.sh 

启动 ranger-admin start

```
[root@master admin]# ranger-admin start
Starting Apache Ranger Admin Service
Apache Ranger Admin Service with pid 15965 has started.
```

本地虚拟机ranger admin http://192.168.1.167:6080   管理员账户admin 密码ranger123

#### 安装ranger-usersync

修改ranger-usersync的install.properties 文件其中的:

```
POLICY_MGR_URL=http://master:6080

#User and group for the usersync process
unix_user=root
unix_group=root

rangerUsersync_password=ranger123

#Set to run in kerberos environment
usersync_principal=hadoop/master@HADOOP.COM
usersync_keytab=/data/hadoop.keytab
hadoop_conf=/data/hadoop-2.8.5/etc/hadoop
```

安装/data/ranger/usersync/setup.sh

启动ranger-usersync start

```
[root@master logs]# ranger-usersync start
Starting Apache Ranger Usersync Service
Apache Ranger Usersync Service with pid 18593 has started.
```

#### 安装ranger-hdfs-plugin

修改ranger-hdfs-plugin的install.properties 文件其中的:

```
POLICY_MGR_URL=http://master:6080

REPOSITORY_NAME=hadoopdev

COMPONENT_INSTALL_DIR_NAME=/data/hadoop-2.8.5
```

启用/data/ranger/hdfs/enable-hdfs-plugin.sh 

进入ranger web页面,点击HDFS，选择+

```
Service Name *  hadoopdev
Active Status   Enabled
Username *		admin
Password *		ranger123
Namenode URL *  	hdfs://master:4007/
Authorization Enabled   	yes
Authentication Type *  		kerberos
hadoop.security.auth_to_local RULE:[2:$1@$0](hadoop/.*@HADOOP.COM)s/.*/hadoop/
```

ranger每一个插件开启kerberos后，拉取策略到本地需要在ranger-admin的web页面创建serveice时增加Add New Configurations如下配置项，value为组件的启动用户

```
Name							 value
policy.download.auth.users       hadoop		
```

点击Test Connection, save

#### ranger-hdfs 测试

设置hdfs /test目录只对所有者有权限

```
[hadoop@master hdfs]$ hdfs dfs -chmod 600 /test
[hadoop@master hdfs]$ hdfs dfs -ls /
Found 7 items
drwxr-xr-x   - hadoop supergroup          0 2021-10-25 01:39 /data
-rw-r--r--   2 hadoop supergroup       2324 2021-10-25 03:13 /hadoop.keytab
drwxr-xr-x   - hadoop supergroup          0 2021-11-02 00:42 /spark-history
drw-------   - hadoop supergroup          0 2021-10-26 07:23 /test
drwxrwx---   - hadoop supergroup          0 2021-10-17 20:10 /tmp
drwxr-xr-x   - hadoop supergroup          0 2021-10-14 18:54 /user
drwxr-xr-x   - hadoop supergroup          0 2021-10-17 23:12 /usr
```

ccc用户无kerberos权限

切换到ccc ,上传文件到/test目录,结果显示无kerberos 权限

```
[ccc@master data]$ hdfs dfs -put test.txt /test
21/11/03 16:44:27 WARN ipc.Client: Exception encountered while connecting to the server : javax.security.sasl.SaslException: GSS initiate failed [Caused by GSSException: No valid credentials provided (Mechanism level: Failed to find any Kerberos tgt)]
put: Failed on local exception: java.io.IOException: javax.security.sasl.SaslException: GSS initiate failed [Caused by GSSException: No valid credentials provided (Mechanism level: Failed to find any Kerberos tgt)]; Host Details : local host is: "master/192.168.1.167"; destination host is: "master":4007; 
```

添加ccc的kerberos权限，再次上传,显示无目录权限

```
[root@master hadoop]# kadmin.local 
Authenticating as principal hadoop/admin@HADOOP.COM with password.
kadmin.local:  addprinc ccc/master
WARNING: no policy specified for ccc/master@HADOOP.COM; defaulting to no policy
Enter password for principal "ccc/master@HADOOP.COM": 
Re-enter password for principal "ccc/master@HADOOP.COM": 
Principal "ccc/master@HADOOP.COM" created.
[ccc@master data]$  kinit ccc/master
Password for ccc/master@HADOOP.COM: 
[ccc@master data]$  hdfs dfs -put test.txt /test
put: Permission denied: user=ccc, access=EXECUTE, inode="/test":hadoop:supergroup:drw-------
```

在ranger web端设置ccc用户对/test目录有全部权限,重启hadoop，再次上传，可以成功，ranger hdfs 插件生效

```
[ccc@master data]$ hdfs dfs -put test.txt /test
[ccc@master data]$ hdfs dfs -ls /test
Found 1 items
-rw-r--r--   1 ccc supergroup          9 2021-11-04 04:20 /test/test.txt
```

#### 安装ranger-hive-plugin

修改ranger-hive-plugin的install.properties 文件其中的:

```
POLICY_MGR_URL=http://master:6080

REPOSITORY_NAME=hivedev

COMPONENT_INSTALL_DIR_NAME=/data/hive-2.3.5
```

开启hive插件/data/ranger/hive/enable-hive-plugin.sh

```
[root@master hive]# ./enable-hive-plugin.sh 
Custom group is available, using default user and custom group.
+ Thu Nov  4 04:43:48 CST 2021 : hive: lib folder=/data/hive-2.3.5/lib conf folder=/data/hive-2.3.5/conf
chown: invalid user: ‘hive:hadoop’
chown: invalid user: ‘hive:hadoop’
chown: invalid user: ‘hive:hadoop’
chown: invalid user: ‘hive:hadoop’
chown: invalid user: ‘hive:hadoop’
+ Thu Nov  4 04:43:48 CST 2021 : Creating default file from [/data/ranger/hive/install/conf.templates/default/configuration.xml] for [/data/hive-2.3.5/conf/hiveserver2-site.xml] ..
chown: invalid user: ‘hive:hadoop’
+ Thu Nov  4 04:43:48 CST 2021 : Saving current config file: /data/hive-2.3.5/conf/hiveserver2-site.xml to /data/hive-2.3.5/conf/.hiveserver2-site.xml.20211104-044348 ...
+ Thu Nov  4 04:43:49 CST 2021 : Saving current config file: /data/hive-2.3.5/conf/ranger-hive-audit.xml to /data/hive-2.3.5/conf/.ranger-hive-audit.xml.20211104-044348 ...
+ Thu Nov  4 04:43:49 CST 2021 : Saving current config file: /data/hive-2.3.5/conf/ranger-hive-security.xml to /data/hive-2.3.5/conf/.ranger-hive-security.xml.20211104-044348 ...
+ Thu Nov  4 04:43:49 CST 2021 : Saving current config file: /data/hive-2.3.5/conf/ranger-policymgr-ssl.xml to /data/hive-2.3.5/conf/.ranger-policymgr-ssl.xml.20211104-044348 ...
+ Thu Nov  4 04:43:50 CST 2021 : Saving current JCE file: /etc/ranger/hivedev/cred.jceks to /etc/ranger/hivedev/.cred.jceks.20211104044350 ...
chown: invalid user: ‘hive:hadoop’
Ranger Plugin for hive has been enabled. Please restart hive to ensure that changes are effective.
```

启动hive元数据和hiveserver2

```
nohup /data/hive-2.3.5/bin/hive --service metastore > /data/hive-2.3.5/metastore.log 2>&1 &
nohup /data/hive-2.3.5/bin/hiveserver2 > /data/hive-2.3.5/hive.log 2>&1 &
```

进入ranger web页面,点击hive，选择+

```
Service Name *  hivedev
Active Status   Enabled
Username *		admin
Password *		ranger123
jdbc.driverClassName *  	org.apache.hive.jdbc.HiveDriver
jdbc.url *		jdbc:hive2://master:7001/;principal=hadoop/master@HADOOP.COM
```

ranger每一个插件开启kerberos后，拉取策略到本地需要在ranger-admin的web页面创建serveice时增加Add New Configurations如下配置项，value为组件的启动用户

```
Name							 value
policy.download.auth.users       hadoop		
```

点击Test Connection, save

#### ranger-hive 测试

无kerkeros权限用户ccc

```
[ccc@master hadoop]$ kdestroy 
[ccc@master hadoop]$ klist
klist: No credentials cache found (filename: /tmp/krb5cc_1005)
```

直接启动beeline无法连接

```
[ccc@master hadoop]$ /data/hive-2.3.5/bin/beeline -u "jdbc:hive2://localhost:7001/default"
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/data/hive-2.3.5/lib/log4j-slf4j-impl-2.6.2.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/data/hadoop-2.8.5/share/hadoop/common/lib/slf4j-log4j12-1.7.10.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [org.apache.logging.slf4j.Log4jLoggerFactory]
Connecting to jdbc:hive2://localhost:7001/default
21/11/04 05:18:03 [main]: WARN jdbc.HiveConnection: Failed to connect to localhost:7001
Unknown HS2 problem when communicating with Thrift server.
Error: Could not open client transport with JDBC Uri: jdbc:hive2://localhost:7001/default: Peer indicated failure: Unsupported mechanism type PLAIN (state=08S01,code=0)
Beeline version 2.3.5 by Apache Hive
```

使用kerberos参数连接

```
[ccc@master hadoop]$ /data/hive-2.3.5/bin/beeline -u "jdbc:hive2://localhost:7001/default;principal=hadoop/master@HADOOP.COM" -n ccc

Caused by: org.ietf.jgss.GSSException: No valid credentials provided (Mechanism level: Failed to find any Kerberos tgt)
        at sun.security.jgss.krb5.Krb5InitCredential.getInstance(Krb5InitCredential.java:147) ~[?:1.8.0_212]
        at sun.security.jgss.krb5.Krb5MechFactory.getCredentialElement(Krb5MechFactory.java:122) ~[?:1.8.0_212]
        at sun.security.jgss.krb5.Krb5MechFactory.getMechanismContext(Krb5MechFactory.java:187) ~[?:1.8.0_212]
        at sun.security.jgss.GSSManagerImpl.getMechanismContext(GSSManagerImpl.java:224) ~[?:1.8.0_212]
        at sun.security.jgss.GSSContextImpl.initSecContext(GSSContextImpl.java:212) ~[?:1.8.0_212]
        at sun.security.jgss.GSSContextImpl.initSecContext(GSSContextImpl.java:179) ~[?:1.8.0_212]
        at com.sun.security.sasl.gsskerb.GssKrb5Client.evaluateChallenge(GssKrb5Client.java:192) ~[?:1.8.0_212]
        ... 36 more
21/11/04 05:19:57 [main]: WARN jdbc.HiveConnection: Failed to connect to localhost:7001
Error: Could not open client transport with JDBC Uri: jdbc:hive2://localhost:7001/default;principal=hadoop/master@HADOOP.COM: GSS initiate failed (state=08S01,code=0)
```

认证kerkeros 获取ticket后再次连接

```
[ccc@master hadoop]$ kinit ccc/master
Password for ccc/master@HADOOP.COM: 
[ccc@master hadoop]$ klist
Ticket cache: FILE:/tmp/krb5cc_1005
Default principal: ccc/master@HADOOP.COM

Valid starting       Expires              Service principal
11/04/2021 05:21:24  02/02/2022 05:21:24  krbtgt/HADOOP.COM@HADOOP.COM
[ccc@master hadoop]$ /data/hive-2.3.5/bin/beeline -u "jdbc:hive2://localhost:7001/default;principal=hadoop/master@HADOOP.COM" -n ccc
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/data/hive-2.3.5/lib/log4j-slf4j-impl-2.6.2.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/data/hadoop-2.8.5/share/hadoop/common/lib/slf4j-log4j12-1.7.10.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [org.apache.logging.slf4j.Log4jLoggerFactory]
Connecting to jdbc:hive2://localhost:7001/default;principal=hadoop/master@HADOOP.COM
Connected to: Apache Hive (version 2.3.5)
Driver: Hive JDBC (version 2.3.5)
Transaction isolation: TRANSACTION_REPEATABLE_READ
Beeline version 2.3.5 by Apache Hive
0: jdbc:hive2://localhost:7001/default> select * from test01.aaa;
Error: Error while compiling statement: FAILED: HiveAccessControlException Permission denied: user [ccc] does not have [SELECT] privilege on [test01/aaa/*] (state=42000,code=40000)
```

在ranger hive-plugin web端增加用户ccc的查表权限，重启hiveserve2服务，再次启动beeline 查询

```
[ccc@master hadoop]$ /data/hive-2.3.5/bin/beeline -u "jdbc:hive2://localhost:7001/default;principal=hadoop/master@HADOOP.COM" -n ccc
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/data/hive-2.3.5/lib/log4j-slf4j-impl-2.6.2.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/data/hadoop-2.8.5/share/hadoop/common/lib/slf4j-log4j12-1.7.10.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [org.apache.logging.slf4j.Log4jLoggerFactory]
Connecting to jdbc:hive2://localhost:7001/default;principal=hadoop/master@HADOOP.COM
Connected to: Apache Hive (version 2.3.5)
Driver: Hive JDBC (version 2.3.5)
Transaction isolation: TRANSACTION_REPEATABLE_READ
Beeline version 2.3.5 by Apache Hive
0: jdbc:hive2://localhost:7001/default>  select * from test01.aaa;
+-----------+--------------+-----------+----------------------------------------------------+
| aaa.code  | aaa.message  | aaa.data  |                   aaa.data_json                    |
+-----------+--------------+-----------+----------------------------------------------------+
| 1000000   | SUCCESS      | success   | {"code":"1000000","data":"success","message":"SUCCESS"} |
| 1000000   | SUCCESS      | success   | {"code":"1000000","data":"success","message":"SUCCESS"} |
| 1000000   | SUCCESS      | success   | {"code":"1000000","data":"success","message":"SUCCESS"} |
| 1000000   | SUCCESS      | success   | {"code":"1000000","data":"success","message":"SUCCESS"} |
| 1000000   | SUCCESS      | success   | {"code":"1000000","data":"success","message":"SUCCESS"} |
| 1000000   | SUCCESS      | success   | {"code":"1000000","data":"success","message":"SUCCESS"} |
| 1000000   | SUCCESS      | success   | {"code":"1000000","data":"success","message":"SUCCESS"} |
| 1000000   | SUCCESS      | success   | {"code":"1000000","data":"success","message":"SUCCESS"} |
| 1000000   | SUCCESS      | success   | {"code":"1000000","data":"success","message":"SUCCESS"} |
| 1000000   | SUCCESS      | success   | {"code":"1000000","data":"success","message":"SUCCESS"} |
| 1000000   | SUCCESS      | success   | {"code":"1000000","data":"success","message":"SUCCESS"} |
| 1000000   | SUCCESS      | success   | {"code":"1000000","data":"success","message":"SUCCESS"} |
| 1000000   | SUCCESS      | success   | {"code":"1000000","data":"success","message":"SUCCESS"} |
| 1000000   | SUCCESS      | success   | {"code":"1000000","data":"success","message":"SUCCESS"} |
| 1000000   | SUCCESS      | success   | {"code":"1000000","data":"success","message":"SUCCESS"} |
| 1000000   | SUCCESS      | success   | {"code":"1000000","data":"success","message":"SUCCESS"} |
| 1000000   | SUCCESS      | success   | {"code":"1000000","data":"success","message":"SUCCESS"} |
| 1000000   | SUCCESS      | success   | {"code":"1000000","data":"success","message":"SUCCESS"} |
| 1000000   | SUCCESS      | success   | {"code":"1000000","data":"success","message":"SUCCESS"} |
| 1000000   | SUCCESS      | success   | {"code":"1000000","data":"success","message":"SUCCESS"} |
| 1000000   | SUCCESS      | success   | {"code":"1000000","data":"success","message":"SUCCESS"} |
+-----------+--------------+-----------+----------------------------------------------------+
21 rows selected (2.4 seconds)
0: jdbc:hive2://localhost:7001/default> 
```

