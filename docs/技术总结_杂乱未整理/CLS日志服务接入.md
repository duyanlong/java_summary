## 采集方式

日志服务提供多种采集方式：

| 采集方式               | 描述                                                         |
| :--------------------- | ------------------------------------------------------------ |
| API 方式采集           | 通过调用 [日志服务 API](https://cloud.tencent.com/document/product/614/12445) 上传结构化日志至日志服务，日志上传参考 [上传日志接口](https://cloud.tencent.com/document/product/614/16873) 文档 |
| SDK 方式采集           | 暂无 SDK 提供                                                |
| LogListener 客户端采集 | LogListener 是日志服务提供的日志采集客户端，通过控制台简单配置可快速接入日志服务，使用方式参考 [LogListener 使用流程](https://cloud.tencent.com/document/product/614/33495) |

采集方式对比：

| 类别名称 | LogListener 采集                   | API 方式采集                   |
| -------- | ---------------------------------- | ------------------------------ |
| 修改代码 | 对应用程序是无侵入式，无需修改代码 | 需修改应用程序代码才能上报日志 |
| 断点续传 | 支持断点续传日志                   | 自行代码实现                   |
| 失败重传 | 自带重试机制                       | 自行代码实现                   |
| 本地缓存 | 支持本地缓存，高峰期间保障数据完整 | 自行代码实现                   |
| 资源占用 | 占用内存、CPU 等资源               | 无额外资源占用                 |

## 日志源接入

不同的日志源可以选择不同的日志接入方式，详情参考以下列表：

**日志源类别**

| 日志源类别   | 推荐接入方式 |
| ------------ | ------------ |
| 程序直接输出 | API          |
| 本地日志文件 | LogListener  |

**日志源环境**

| 系统环境    | 推荐接入方式                        |
| ----------- | ----------------------------------- |
| Linux/Unix  | LogListener                         |
| Windows     | API（LogListener 暂不支持 Windows） |
| iOS/Android | API（暂无 iOS/Android SDK）         |

## 用户身份授权
在登录后 个人头像-安全管理-访问秘钥-api秘钥管理  页面新建个人秘钥，公司账号使用运维统一授权秘钥
后面secretid secretKey使用此处授权

## 客户端方式
### 客户端下载安装
#### 外网下载：浏览器访问下面地址下载日志服务客户端安装包
```shell
wget https://mirrors.tencent.com/install/cls/loglistener-linux-x64-2.8.3.tar.gz  && tar -zxvf loglistener-linux-x64-2.8.3.tar.gz -C /usr/local && cd /usr/local/loglistener-2.8.3/tools && ./loglistener.sh install
```
#### tencent CVM内网下载：命令行 wget 下载
```shell 
wget http://mirrors.tencentyun.com/install/cls/loglistener-linux-x64-2.8.3.tar.gz  && tar -zxvf loglistener-linux-x64-2.8.3.tar.gz -C /usr/local && cd /usr/local/loglistener-2.8.3/tools && ./loglistener.sh install
```

### 初始化 LogListener
```shell 
./loglistener.sh init -secretid xxxx -secretkey xxxx -region ap-xxxxxx
```

#### 参数说明
| 参数名    | 类型描述                                                     |
| --------- | ------------------------------------------------------------ |
| secretid  | [云 API 密钥](https://console.cloud.tencent.com/cam/capi) 的一部分，SecretId 用于标识 API 调用者身份 |
| secretkey | [云 API 密钥](https://console.cloud.tencent.com/cam/capi) 的一部分，SecretKey 是用于加密签名字符串和服务器端验证签名字符串的密钥 |
| region    | region 表示日志服务所在的 [地域](https://cloud.tencent.com/document/product/614/18940)，此处填写域名简称，例如 ap-beijing、ap-guangzhou 等 |
| network   | 表示 loglistener 通过哪种方式访问服务域名，取值：intra 内网访问（默认），internet 外网访问 |
| ip        | 机器的 IP 标识。若不填写，loglistener 会自动获取本机的 IP 地址 |
| label     | 机器组标示，标示机器组需要填写标示信息，多个标示按逗号分隔 |

如果需要通过外网方式访问服务域名，需要显式设置网络参数internet，执行如下命令
```shell 
./loglistener.sh init -secretid xxxx -secretkey xxx -region ap-xxxxxx -network internet
```

#### 可用地域及简称 上面的region
| 地域     | 域名简称         | 内网域名                            | 外网域名                           |
| :------- | :--------------- | :---------------------------------- | :--------------------------------- |
| 北京     | ap-beijing       | ap-beijing.cls.tencentyun.com       | ap-beijing.cls.tencentcs.com/ap-beijing.cls.tencentcs.cn       |
| 广州     | ap-guangzhou     | ap-guangzhou.cls.tencentyun.com     | ap-guangzhou.cls.tencentcs.com/ap-guangzhou.cls.tencentcs.cn     |
| 上海     | ap-shanghai      | ap-shanghai.cls.tencentyun.com      | ap-shanghai.cls.tencentcs.com/ap-shanghai.cls.tencentcs.cn      |
| 成都     | ap-chengdu       | ap-chengdu.cls.tencentyun.com       | ap-chengdu.cls.tencentcs.com/ap-chengdu.cls.tencentcs.cn       |
| 南京     | ap-nanjing       | ap-nanjing.cls.tencentyun.com       | ap-nanjing.cls.tencentcs.com/ap-nanjing.cls.tencentcs.cn       |
| 重庆     | ap-chongqing     | ap-chongqing.cls.tencentyun.com     | ap-chongqing.cls.tencentcs.com/ap-chongqing.cls.tencentcs.cn     |
| 中国香港 | ap-hongkong      | ap-hongkong.cls.tencentyun.com      | ap-hongkong.cls.tencentcs.com/ap-hongkong.cls.tencentcs.cn      |
| 硅谷     | na-siliconvalley | na-siliconvalley.cls.tencentyun.com | na-siliconvalley.cls.tencentcs.com/na-siliconvalley.cls.tencentcs.cn |
| 弗吉尼亚 | na-ashburn       | na-ashburn.cls.tencentyun.com       | na-ashburn.cls.tencentcs.com/na-ashburn.cls.tencentcs.cn |
| 新加坡   | ap-singapore     | ap-singapore.cls.tencentyun.com     | ap-singapore.cls.tencentcs.com/ap-singapore.cls.tencentcs.cn     |
| 泰国  | ap-bangkok   | ap-bangkok.cls.tencentyun.com  | ap-bangkok.cls.tencentcs.com/ap-bangkok.cls.tencentcs.cn  |
| 孟买     | ap-mumbai        | ap-mumbai.cls.tencentyun.com        | ap-mumbai.cls.tencentcs.com/ap-mumbai.cls.tencentcs.cn   |
| 法兰克福 | eu-frankfurt     | eu-frankfurt.cls.tencentyun.com     | eu-frankfurt.cls.tencentcs.com/eu-frankfurt.cls.tencentcs.cn   |  
| 东京     | ap-tokyo         | ap-tokyo.cls.tencentyun.com         | ap-tokyo.cls.tencentcs.com/ap-tokyo.cls.tencentcs.cn         |
| 首尔     | ap-seoul         | ap-seoul.cls.tencentyun.com         | ap-seoul.cls.tencentcs.com/ap-seoul.cls.tencentcs.cn         |
| 莫斯科  | eu-moscow   |  eu-moscow.cls.tencentyun.com  |  eu-moscow.cls.tencentcs.com/eu-moscow.cls.tencentcs.cn  |
| 深圳金融 | ap-shenzhen-fsi  | ap-shenzhen-fsi.cls.tencentyun.com  | ap-shenzhen-fsi.cls.tencentcs.com/ap-shenzhen-fsi.cls.tencentcs.cn  |
| 上海金融 | ap-shanghai-fsi  | ap-shanghai-fsi.cls.tencentyun.com  | ap-shanghai-fsi.cls.tencentcs.com/ap-shanghai-fsi.cls.tencentcs.cn  |
| 北京金融 | ap-beijing-fsi  | ap-beijing-fsi.cls.tencentyun.com  | ap-beijing-fsi.cls.tencentcs.com/ap-beijing-fsi.cls.tencentcs.cn  |

### 启动LogLister
```shell
检查客户端状态
/etc/init.d/loglistenerd check
启动客户端
/etc/init.d/loglistenerd start
```

### 文件接入多个数据集方式
* 默认情况下，一个文件只能被一个 LogListener 配置采集。
* 如果一个文件需对应多个采集配置，请给源文件添加一个软链接，并将其加到另一组采集配置中。
* LogListener 2.3.9及以上版本才可以添加多个采集路径。

### 批量安装客户端
支持批量安装方式，可在腾讯云日志服务控制台 在快速接入 > 云产品日志栏中，单击云服务器CVM  选择批量接入；
**前提条件：** CVM 已 安装腾讯云自动化助手（TencentCloud Automation Tools，TAT）

#### 安装命令集
```shell
wget http://mirrors.tencentyun.com/install/cls/loglistener-linux-x64-2.8.3.tar.gz  && tar -zxvf loglistener-linux-x64-2.8.3.tar.gz -C /usr/local && cd /usr/local/loglistener-2.8.3/tools && ./loglistener.sh install
sh loglistener.sh init -secretid xxxx -secretkey xxxx -region ap-guangzhou
/etc/init.d/loglistenerd start
/etc/init.d/loglistenerd check
```

## 管理台配置
![img.png](img.png)
### 客户端列表管理
在日志服务CLS控制台中，机器组管理-新建机器组  中配置安装好后的客户端IP列表就可以将客户端管理到日志服务

### 日志主题
日志主题-创建日志主题 ，主题挂在日志集下；一个日志集下有多个日志主题，日志主题对应ES type,日志集对应 ES index;
在新建的日志主题中进行采集配置，选择对应的机器组，并配置相应监听日志文件目录；