## dolphinscheduler 添加 kerberos 配置

sed -i 's/hadoop.security.authentication.startup.state=false/hadoop.security.authentication.startup.state=true/g' /data/dolphinscheduler/*/conf/*
sed -i 's/\/opt\/krb5.conf/\/etc\/krb5.conf/g' /data/dolphinscheduler/*/conf/*
sed -i 's/hdfs-mycluster@ESZ.COM/hadoop\/xx.xx.xxx.xx@EMR-5XXB2AC5/g' /data/dolphinscheduler/*/conf/*
sed -i 's/\/opt\/hdfs.headless.keytab/\/var\/krb5kdc\/emr.keytab/g' /data/dolphinscheduler/*/conf/*

sed -i 's/hadoop.security.authentication.startup.state=false/hadoop.security.authentication.startup.state=true/g' /data/ds_conf/*/conf/*
sed -i 's/\/opt\/krb5.conf/\/etc\/krb5.conf/g' /data/ds_conf/*/conf/*
sed -i 's/hdfs-mycluster@ESZ.COM/hadoop\/_hadoop\/xx.xx.xxx.xx@EMR-5XXB2AC5/g' /data/ds_conf/*/conf/*
sed -i 's/\/opt\/hdfs.headless.keytab/\/var\/krb5kdc\/emr.keytab/g' /data/ds_conf/*/conf/*

sed -i 's/hadoop.security.authentication.startup.state=false/hadoop.security.authentication.startup.state=true/g' /data/dolphinscheduler/install/*/conf/common.properties
sed -i 's/\/opt\/krb5.conf/\/etc\/krb5.conf/g' /data/dolphinscheduler/install/*/conf/common.properties
sed -i 's/hdfs-mycluster@ESZ.COM/hadoop\/xx.xx.xxx.xx@EMR-5XXB2AC5/g' /data/dolphinscheduler/install/*/conf/common.properties
sed -i 's/\/opt\/hdfs.headless.keytab/\/var\/krb5kdc\/emr.keytab/g' /data/dolphinscheduler/install/*/conf/common.properties 


https://cloud.tencent.com/document/product/589/35065
```text
## 替换后结果 dolphinscheduler/worker/conf/common.properties

# whether to startup kerberos
hadoop.security.authentication.startup.state=true

# java.security.krb5.conf path
java.security.krb5.conf.path=/etc/krb5.conf

# login user from keytab username
login.user.keytab.username=hadoop/xx.xx.xxx.xx@EMR-5XXB2AC5
# 这里的 username 可以通过客户端执行 klist 查看可用 principal

# login user from keytab path
login.user.keytab.path=/var/krb5kdc/emr.keytab

# kerberos expire time, the unit is hour
kerberos.expire.time=2


## hadoop.security.authentication.startup.state	false	hadoop是否开启kerberos权限
## java.security.krb5.conf.path	/opt/krb5.conf	kerberos配置目录
## login.user.keytab.username	hdfs-mycluster@ESZ.COM	kerberos登录用户
## login.user.keytab.path	/opt/hdfs.headless.keytab	kerberos登录用户keytab
## kerberos.expire.time	2	kerberos过期时间,整数,单位为小时
```



