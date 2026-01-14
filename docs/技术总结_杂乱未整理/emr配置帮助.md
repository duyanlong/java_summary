### 修复knox证书过期
mv /usr/local/service/knox/data/security/keystores/gateway.jks /usr/local/service/knox/data/security/keystores/gateway.jks_bak
/usr/local/service/knox/bin/ldap.sh stop
/usr/local/service/knox/bin/ldap.sh start
/usr/local/service/knox/bin/gateway.sh stop
/usr/local/service/knox/bin/gateway.sh start

### knox（emr 服务管理agent）
su hadoop -c " /usr/local/service/knox/bin/ldap.sh stop;/usr/local/service/knox/bin/ldap.sh start; /usr/local/service/knox/bin/gateway.sh stop; /usr/local/service/knox/bin/gateway.sh start"
上面的文件在各个组件的控制台新增，文件都有一个配置项，需要改成您的ranger ip哈，改成http://xx.xx.xxx.xx:6080
1、添加配置文件ranger-hive-securety.xml
2、在文件中添加  ranger uri这个属性
3、重启hive或hbase
重启后文件内的其他配置和ranger-hive-audit.xml、ranger-hive-policymagr-ssl.xml两个文件自动就有了是吗
上面有发两个配置文件，一个是hive的一个是hbase的，都需要更改下ranger ip，进行加载，加载完了再进行重启下哈
