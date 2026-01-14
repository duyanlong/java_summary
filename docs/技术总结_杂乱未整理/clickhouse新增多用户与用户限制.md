## clickhouse新增多用户与用户限制

clickhouse可以使用配置文件和sql-driven新增用户和用户权限管理

**[访问权限和账户管理] (https://clickhouse.tech/docs/zh/operations/access-rights/#access-rights)**

配置实例： https://www.cnblogs.com/gentlescholar/p/15043329.html

使用配置文件限制，限制多用户，可以以为每个用户单独配置一个xml文件的方式，存放在/etc/clickhouse-server/users.d目录，修改完权限文件之后ClickHouse不需要重启，直接会生效

20.5版本增加sql-driven,目前版本20.3没有systerm.user等一系列表，无法实现sql grant限制

Clickhouse20.5.2.7新特性 官方文档：

https://clickhouse.com/docs/en/whats-new/changelog/2020/#new-feature_7

Clickhouse20.5.2.7新特性中文翻译：

http://hohode.com/2020/07/29/Clickhouse%E6%96%B0%E7%89%B9%E6%80%A7/



#### 使用配置文件进行用户管理

配置参考

http://www.weijingbiji.com/1790/

在/etc/clickhouse-server/user.d/目录下新增 aa.xml用户权限文件用来配置新用户权限
```xml
<yandex>
 
     <!-- Users and ACL. -->
     <users>
         <!-- If user name was not specified, 'default' user is used. -->
         <hdp-hr>
             <password_sha256_hex>eec1cd01493868cdd9c158606a7005498ff3a359b495c7c2389d76916fd7af87</password_sha256_hex>

	     	 <access_management>1</access_management>
             <networks incl="networks" replace="replace">
                 <ip>::/0</ip>
             </networks>
 
             <!-- Settings profile for user. -->
             <profile>default</profile>
 
             <!-- Quota for user. -->
             <quota>default</quota>
	     <allow_databases>
		<database>HDP_QA_DWS</database>
	     </allow_databases>
         </hdp-hr>

     </users>
 
 </yandex>
```


#### 生产sha256加密密码
sha1
base64 < /dev/urandom | head -c8;echo "12345678"; echo -n "12345678" | sha1sum | tr -d '-' | xxd -r -p | sha1sum | tr -d '-'
sha256 
base64 < /dev/urandom | head -c8； echo "12345678"; echo -n "12345678" | sha256sum | tr -d '-'

### 常见问题
* 需确保users.xml及users.d目录下权限配置文件权限正确，必须644权限

