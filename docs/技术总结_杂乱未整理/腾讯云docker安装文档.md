### 1、较旧的 Docker 版本称为 docker 或 docker-engine 。如果已安装这些程序，请卸载它们以及相关的依赖项

```
sudo yum remove docker \
                  docker-client \
                  docker-client-latest \
                  docker-common \
                  docker-latest \
                  docker-latest-logrotate \
                  docker-logrotate \
                  docker-engine
```



### 2、安装所需的软件包。yum-utils 提供了 yum-config-manager ，并且 device mapper 存储驱动程序需要 device-mapper-persistent-data 和 lvm2

```
sudo yum install -y yum-utils \
  device-mapper-persistent-data \
  lvm2
```

使用以下命令来设置稳定的仓库。

**使用官方源地址（比较慢）**

```
$ sudo yum-config-manager \
    --add-repo \
    https://download.docker.com/linux/centos/docker-ce.repo
```

可以选择国内的一些源地址：

**阿里云**

```
$ sudo yum-config-manager \
    --add-repo \
    http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
```

**清华大学源**

```
$ sudo yum-config-manager \
    --add-repo \
    https://mirrors.tuna.tsinghua.edu.cn/docker-ce/linux/centos/docker-ce.repo
```



### 3、安装最新版本的 Docker Engine-Community 和 containerd

```
 sudo yum install docker-ce docker-ce-cli containerd.io
```



### 4、如果上面出现下面报错信息，可以执行一个命令可解决

failure: repodata/repomd.xml from docker-ce-stable: [Errno 256] No more mirrors to try.
https://download.docker.com/linux/centos/2.2/x86_64/stable/repodata/repomd.xml: [Errno 14] HTTPS Error 404 - Not Found

解决报错命令

```
yum-config-manager --save --setopt=docker-ce-stable.skip_if_unavailable=true
```

运行完成后重复执行步骤3的命令



### 5、启动 Docker

```
sudo systemctl start docker
```

测试

```
sudo docker run hello-world
```

### 6、修改Docker 容器位置

参考

https://blog.csdn.net/weixin_32820767/article/details/81196250

```
#停止docker服务
systemctl stop docker
#创建新的docker目录
mkdir /data/docker
#迁移/var/lib/docker目录下面的文件到新的docker目录
rsync -avz /var/lib/docker/* /data/docker/
#配置 /etc/systemd/system/docker.service.d/devicemapper.conf。查看 devicemapper.conf 是否存在。如果不存在，就新建
mkdir -p /etc/systemd/system/docker.service.d/
```

在 devicemapper.conf 写入

```
sudo vim /etc/systemd/system/docker.service.d/devicemapper.conf
```

```
[Service]
ExecStart=
ExecStart=/usr/bin/dockerd  --graph=/data/docker
```

重新加载 docker

```
systemctl daemon-reload
systemctl restart docker
systemctl enable docker
```

 确保配置生效，命令查看

```
docker info
```

可以在信息里找到：

```
Docker Root Dir: /data/docker
```

删除原docker目录

```
cd /var/lib/docker && rm -rf /var/lib/docker
```

### 6、修改Docker 容器位置

参考

https://blog.csdn.net/weixin_32820767/article/details/81196250

```
#停止docker服务
systemctl stop docker
#创建新的docker目录
mkdir /data/docker
#迁移/var/lib/docker目录下面的文件到新的docker目录
rsync -avz /var/lib/docker /data/docker
#配置 /etc/systemd/system/docker.service.d/devicemapper.conf。查看 devicemapper.conf 是否存在。如果不存在，就新建
mkdir -p /etc/systemd/system/docker.service.d/
```

在 devicemapper.conf 写入

```
sudo vim /etc/systemd/system/docker.service.d/devicemapper.conf
```

```
[Service]
ExecStart=
ExecStart=/usr/bin/dockerd  --graph=/data/docker
```

重新加载 docker

```
systemctl daemon-reload
systemctl restart docker
systemctl enable docker
```

 确保配置生效，命令查看

```
docker info
```

可以在信息里找到：

```
Docker Root Dir: /data/docker
```

删除原docker目录

```
cd /var/lib/docker && rm -rf /var/lib/docker
```

## docker如pull 镜像失败则可修改配置
访问 https://registry-1.docker.io/v2/ 报错 UNAUTHORIZED
cat /etc/docker/daemon.json
```json
{
"registry-mirrors" : [
"http://mirrors-docker.tmeoa.com",
"https://mirror.ccs.tencentyun.com"
]
}
```