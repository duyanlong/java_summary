### 1、安装docker

详情请参考《腾讯云docker安装文档》



### 2、Docker Compose安装

运行以下命令以下载 Docker Compose 的当前稳定执行Airflow的版本：

```
sudo curl -L https://github.com/docker/compose/releases/download/1.29.1/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose
```

将可执行权限应用于二进制文件：

```
sudo chmod +x /usr/local/bin/docker-compose
```

创建软链：

```
sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
```



### 3、Docker 安装redis

现在docker-compose会自动拉取安装redis，不需要自己安装redis

```
--docker pull redis:latest
--docker images
--docker run -itd --name redis-test -p 6379:6379 redis
```

如redis异常关闭，可以使用以下命令进行重启

```
docker restart redis-test
```



### 4、 拷贝镜像到新的服务器

xx.xx.xxx.xx /home/airflow/airflow-tmeit-v2.1.2.tar

堡垒机登录

```
scp -r root@xx.xx.xxx.xx:/home/airflow/ /home/当前登录用户目录
```

复制到uat测试环境xx.xx.xxx.xx

```
scp -r /home/当前登录用户目录/airflow xx.xx.xxx.xx:/data/emr/
```

复制到uat开发环境xx.xx.xxx.xx

```
scp -r /home/当前登录用户目录/airflow xx.xx.xxx.xx:/data/emr/
```



### 5、把airfllow镜像加载在docker里

```
docker load -i /data/emr/airflow/airflow-tmeit-v2.1.2.tar
```

手动修改airflow使用的中台sdk 脚本conf文件配置

uat测试环境

vim /data/cfs/airflow/dags/sdk/middle_platform/config.cfg 

修改以下


### 6、新服务器挂载cfs

```
uat测试环境
mount -t nfs -o vers=4.0,noresvport xx.xx.xxx.xx:/ /data/cfs
uat开发环境
mount -t nfs -o vers=4.0,noresvport xx.xx.xxx.xx:/ /data/cfs
```
mount -t nfs -o vers=4.0,noresvport xx.xx.xxx.xx:/starrocks /data/cfs



### 7、在 /data/cfs 下拉取代码

```
cd /data/cfs && git clone -b develop http://git.tmeoa.com/TMEIT/public/airflow.git
```

手动修改airflow使用的中台sdk 脚本conf文件配置

uat测试环境

vim /data/cfs/airflow/dags/sdk/middle_platform/config.cfg 

修改以下

```
hiveUris=thrift://xx.xx.xxx.xx:7004
serverUris=http://xx.xx.xxx.xx:8001/calc/
```

uat开发环境

vim /data/cfs/airflow/dags/sdk/middle_platform/config.cfg 

修改以下

```
hiveUris=thrift://xx.xx.xxx.xx:7004
serverUris=http://xx.xx.xxx.xx:8001/calc/
```

### 8、在/data/cfs/airflow执行启动命令

创建airflow账号并且赋值docker权限

```
useradd -m airflow

# 分配到hadoop组让其有操作hadoop组权限
usermod -G hadoop airflow 

# 将当前用户添加到docker组
sudo gpasswd -a airflow docker

# 重启docker服务
sudo service docker restart

```

授权数据库给airflow

```
CREATE DATABASE IF NOT EXISTS airflow DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
set global explicit_defaults_for_timestamp =1;  要设置此步，否则后报错
```

修改配置

```
cp /data/cfs/airflow.env-example /data/cfs/airflow.env                  # 修改相应的配置
cp /data/cfs/airflow/airflow.cfg-example /data/cfs/airflow/airflow.cfg    # 修改相应的配置
```

测试uat 

vim /data/cfs/airflow/.env

```
AIRFLOW__CORE__SQL_ALCHEMY_CONN=mysql://airflow:!d7DmzHRhtWA@xx.xx.xxx.xx:3306/airflow?charset=utf8
AIRFLOW__CELERY__RESULT_BACKEND=db+mysql://airflow:!d7DmzHRhtWA@xx.xx.xxx.xx:3306/airflow
AIRFLOW__CELERY__BROKER_URL=redis://:@xx.xx.xxx.xx:6379/0

AIRFLOW__CORE__LOAD_EXAMPLES=false
CELERY_QUEUE_NAME=default
DAG_FOLDER=/data/cfs/airflow/dags
LOG_FOLDER=/data/cfs/airflow/logs
HDFS_SITE=/usr/local/service/hadoop/etc/hadoop/hdfs-site.xml
CORE_SITE=/usr/local/service/hadoop/etc/hadoop/core-site.xml
MAPRED_SITE=/usr/local/service/hadoop/etc/hadoop/mapred-site.xml
YARN_SITE=/usr/local/service/hadoop/etc/hadoop/yarn-site.xml
#HIVE_FOLDER=/usr/local/service/hive/conf


WEB_SERVER_PORT=8081
AIRFLOW_IMAGE_NAME=airflow-tmeit:2.1.2
#AIRFLOW_UID=50000
#AIRFLOW_GID=50000
```

vim /data/cfs/airflow/airflow.cfg

```
修改以下 如果需要airlfow域名访问，将ip地址替换成airlfow域名
base_url = http://xx.xx.xxx.xx:8081
tmeoa_login_url = http://xx.xx.xxx.xx:8081/login
```

如需停止


开发uat

vim /data/cfs/airflow/.env

```
AIRFLOW__CORE__SQL_ALCHEMY_CONN=mysql://airflow:%m&!cX8xQASF@xx.xx.xxx.xx:3306/airflow?charset=utf8
AIRFLOW__CELERY__RESULT_BACKEND=db+mysql://airflow:%m&!cX8xQASF@xx.xx.xxx.xx:3306/airflow
AIRFLOW__CELERY__BROKER_URL=redis://:@xx.xx.xxx.xx:6379/0

AIRFLOW__CORE__LOAD_EXAMPLES=false
CELERY_QUEUE_NAME=default
DAG_FOLDER=/data/cfs/airflow/dags
LOG_FOLDER=/data/cfs/airflow/logs
HDFS_SITE=/usr/local/service/hadoop/etc/hadoop/hdfs-site.xml
CORE_SITE=/usr/local/service/hadoop/etc/hadoop/core-site.xml
MAPRED_SITE=/usr/local/service/hadoop/etc/hadoop/mapred-site.xml
YARN_SITE=/usr/local/service/hadoop/etc/hadoop/yarn-site.xml
#HIVE_FOLDER=/usr/local/service/hive/conf


WEB_SERVER_PORT=8081
AIRFLOW_IMAGE_NAME=airflow-tmeit:2.1.2
#AIRFLOW_UID=50000
#AIRFLOW_GID=50000
```

vim /data/cfs/airflow/airflow.cfg

```
修改以下 如果需要airlfow域名访问，将ip地址替换成airlfow域名
base_url = http://xx.xx.xxx.xx:8081
tmeoa_login_url = http://xx.xx.xxx.xx:8081/login
```

```
chmod -R 777 /data/cfs/airflow/dags /data/cfs/airflow/logs /data/cfs/airflow/plugins /data/cfs/airflow/airflow_source /data/cfs/airflow/airflow.cfg
chown -R airflow:airflow /data/cfs/airflow/
```

新机器安装启动步骤：

```
su airflow
#初始化数据库，权限等等
cd /data/cfs/airflow/ && docker-compose up airflow-init
#启动 webserver,scheduler worker等容器
cd /data/cfs/airflow/ && docker-compose -f docker-compose-dev.yaml up -d airflow-webserver airflow-scheduler airflow-worker
```

```text
# .env 文件配置
AIRFLOW__CORE__SQL_ALCHEMY_CONN=mysql://airflow:JC989lkjkj@xx.xx.xxx.xx:3306/airflow?charset=utf8
AIRFLOW__CELERY__RESULT_BACKEND=db+mysql://airflow:JC989lkjkj@xx.xx.xxx.xx:3306/airflow
AIRFLOW__CELERY__BROKER_URL=redis://:@xx.xx.xxx.xx:6379/0

AIRFLOW__CORE__LOAD_EXAMPLES=false
CELERY_QUEUE_NAME=default
DAG_FOLDER=/data/cfs/airflow/dags
LOG_FOLDER=/data/cfs/airflow/logs
HDFS_SITE=/usr/local/service/hadoop/etc/hadoop/hdfs-site.xml
CORE_SITE=/usr/local/service/hadoop/etc/hadoop/core-site.xml
MAPRED_SITE=/usr/local/service/hadoop/etc/hadoop/mapred-site.xml
YARN_SITE=/usr/local/service/hadoop/etc/hadoop/yarn-site.xml
#HIVE_FOLDER=/usr/local/service/hive/conf

WEB_SERVER_PORT=8081
AIRFLOW_IMAGE_NAME=airflow-tmeit:2.1.2
#AIRFLOW_UID=50000
#AIRFLOW_GID=50000
```

用airflow账号进行启动操作

```
cd /data/cfs/airflow/ && docker-compose up -d
```

如需停止

```
cd /data/cfs/airflow/ && docker-compose stop
```

### 9、使用Nginx配置域名访问

airlfow域名需申请，替换配置文件中域名并启动nginx

nginx安装

```
cd /usr/local/src/
wget http://nginx.org/download/nginx-1.6.2.tar.gz
tar zxvf nginx-1.6.2.tar.gz
yum -y install make zlib zlib-devel gcc-c++ libtool  openssl openssl-devel
yum install pcre pcre-devel -y 
cd /usr/local/src/nginx-1.6.2
./configure --prefix=/usr/local/webserver/nginx --with-http_stub_status_module --with-http_ssl_module 
make
make install
/usr/local/webserver/nginx/sbin/nginx -v
```

 vim /usr/local/webserver/nginx/conf/nginx.conf

倒数第二行插入include vhosts/*.conf;

如下

```
    #    location / {
    #        root   html;
    #        index  index.html index.htm;
    #    }
    #}
    include vhosts/*.conf;
}
```

```
mkdir /usr/local/webserver/nginx/conf/vhosts
```

测试uat

```
vim /usr/local/webserver/nginx/conf/vhosts/airflow.conf
```

```
server {
    listen 80;
    server_name airlfow域名 xx.xx.xxx.xx;
    proxy_connect_timeout 600;
    proxy_send_timeout 600;
    proxy_read_timeout 600;
    location / {
        proxy_pass http://127.0.0.1:8081;
        access_log logs/access_airflow.log;
        error_log logs/error_airflow.log;
    }
}
```

```
vim /usr/local/nginx/conf/vhosts/flower.conf
```

```
server {
    listen 5556;
    server_name airlfow域名 xx.xx.xxx.xx;
    proxy_connect_timeout 600;
    proxy_send_timeout 600;
    proxy_read_timeout 600;
    location / {
        proxy_pass http://127.0.0.1:5555;
        access_log logs/access_airflow.log;
        error_log logs/error_airflow.log;
    }
}
```

开发uat

```
vim /usr/local/webserver/nginx/conf/vhosts/airflow.conf
```

```
server {
    listen 80;
    server_name airlfow域名 xx.xx.xxx.xx;
    proxy_connect_timeout 600;
    proxy_send_timeout 600;
    proxy_read_timeout 600;
    location / {
        proxy_pass http://127.0.0.1:8081;
        access_log logs/access_airflow.log;
        error_log logs/error_airflow.log;
    }
}
```

```
vim /usr/local/nginx/conf/vhosts/flower.conf
```

```
server {
    listen 5556;
    server_name airlfow域名 xx.xx.xxx.xx;
    proxy_connect_timeout 600;
    proxy_send_timeout 600;
    proxy_read_timeout 600;
    location / {
        proxy_pass http://127.0.0.1:5555;
        access_log logs/access_airflow.log;
        error_log logs/error_airflow.log;
    }
}
```

```
启动nginx
 /usr/local/webserver/nginx/sbin/nginx
```

```
查看nginx启动
ps aux | grep nginx
如果需要停止nginx
/usr/local/webserver/nginx/sbin/nginx -s quit
```



### 10、进入docker的Airflow的work环境进行python模块的安装

```
docker ps
```

通过docker ps 找到NAMES为**airflow_airflow-worker_1**的CONTAINER ID

```
docker exec -it 5696a1f74550 /bin/bash
pip3 install --upgrade pip -i https://mirrors.aliyun.com/pypi/simple/
pip3 install pandas  -i https://mirrors.aliyun.com/pypi/simple/
pip3 --default-timeout=1000 install -U impyla  -i https://mirrors.aliyun.com/pypi/simple/
pip3 install openpyxl  -i https://mirrors.aliyun.com/pypi/simple/
pip3 install hdfs  -i https://mirrors.aliyun.com/pypi/simple/
```

通过docker ps 找到NAMES为**airflow_airflow-webserver_1**的CONTAINER ID

```
docker exec -it 5696a1f74550 /bin/bash
pip3 install hdfs  -i https://mirrors.aliyun.com/pypi/simple/
```

### 11、系统维护

* 新增用户
当前airflow二次开发已接入统一登录，支持微信扫描后直接根据登录信息检查airflow中如没有该用户自动注册，有的话直接登录；用户无需再注册
* 修改用户权限
当前airflow版本有bug无法通过页面修改权限；需要后台将user表中用户参照role表中角色，在ab_userrole表中将用户和用户想要授予的角色权限插入即可;

### 12、airflow访问

```
测试uat
http://xx.xx.xxx.xx:8081/home
开发uat
http://xx.xx.xxx.xx:8081/home
```

排查问题

```
docker logs -f   容器ID
```


### 12、airflow访问

```
测试uat
http://xx.xx.xxx.xx:8081/home
开发uat
http://xx.xx.xxx.xx:8081/home
```

排查问题

```
docker logs -f   容器ID
```

## 重启Airflow
```shell
# 修改Airflow访问地址和登录地址 修改其中 base_url、tmeoa_login_url
# /data/cfs/airflow/airflow.cfg
# 先停止
cd /data/cfs/airflow/ && docker-compose stop
# 再启动
cd /data/cfs/airflow/ && docker-compose -f docker-compose-dev.yaml up -d
```

问题记录：
1. 之前未airflow 突然访问失败了，通过分析日志和排查配置发现是智能网关禁止 ip 访问，而之前配置的  /data/cfs/airflow/airflow.cfg 中 base_url = http://xx.xx.xxx.xx:8081  使用的 ip；
解决方案：将其改为域名访问  base_url = http://dag.test.tmeoa.com 即可；  tmeoa_login_url = http://dag.test.tmeoa.com/login  tmeoa_passport_url = http://passport.test.tmeoa.com
2. 20251209 airflow 无法正常访问，现象为： 1.通过域名 dag.test.tmeoa.com 和 ip+端口 xx.xx.xxx.xx:8081 均无法访问； 2. 连接 mysql在宿主机正常、在 container 中无法访问； 3. 状态未 unhealthly 状态；healthcheck 为 curl http://localhost:8080 4. docker exec -it 进入container 执行 webserver 命令无法识别；
解决方案：
重启 docker   sudo service docker restart
使用 airflow账号登录  su airflow  ; cd /data/cfs/airflow/ && docker-compose -f docker-compose-dev.yaml up -d