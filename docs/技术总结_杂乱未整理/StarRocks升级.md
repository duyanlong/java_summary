## Starrocks 技术验证点和结论：
1. 一个 manager 是否可以管理多个 SR 集群 @延龙；
   不可以；  但有 manager hub 产品，一个 hub 可以管理多个 manager；

2. 当前 SR 集群有访问 IP 都来源于哪里是否可列举；   @琦哥
   个人：IOA + VPN 、 TMEIOA、堡垒机；
   应用：会计引擎应用、管报应用、运维平台、SR 财经 APP 集群、 CLS负载均衡/智能网关、数据开发平台、数据资产平台、数据资产、数据共享、数据展示平台；

3. 新建集群+ 复制旧集群元数据 + ip 切换方案可行性  @琦哥
   社区：集群中节点切换 ip 后 manager 不能用；切换时如回滚到原 IP数据可能不可用；
   我验证：1. Manager  需要重新输入 licenses； 2. 新机器 fe 、 cn 在 manager 中看不到，无法识别；3. sr 集群计算、存储服务没什么影响； 流程易出错；

4. FE3.1 CN 3.3是否可正常运行 @黎明、俊杰
   社区：不可以，建议升级一致；
   实测：企业版步骤不允许； 社区开源版支持，简单测试未出现异常，但不知是否有隐藏 bug；

5. SR manager 迁移到其他服务器  @延龙
   可以，文文已给操作手册；亲测可以，可同时保留两个 manager；

6. 3.1 外表方式访问 3.3 是否可行
   可行，但不建议， jdbc catalog 查询是单线程的；
   实测： 3.1 与 3.3 跨版本 catalog 访问都是可以的；

7. 扩缩容期间 fe 偶数情况验证
   扩容操作时，会检查扩容后FE fowller 节点不允许为偶数；缩容操作时允许降级后为偶数；


## SR开发环境 xx.xx.xxx.xx
/etc/fstab 中添加 mount 开机挂载
xx.xx.xxx.xx:/starrocks /data/cfs nfs vers=3,nolock,proto=tcp,hard,timeo=600,retrans=2,_netdev,noresvport 0 0

元数据备份路径和脚本
mount -t nfs -o vers=4.0,noresvport xx.xx.xxx.xx:/starrocks /data/cfs
 生成每日备份脚本
echo '10 0 * * * root /data/cfs/sr_ddl_recovery/daily_backup.sh >> /data/cfs/sr_ddl_recovery/daily.log 2>&1 ' >> /etc/cron.d/sr_bak_daily
 生成小时备份脚本
echo '2 * * * * root /data/cfs/sr_ddl_recovery/hourly_backup.sh >> /data/cfs/sr_ddl_recovery/hourly.log 2>&1 ' >> /etc/cron.d/sr_bak_hourly
开源版本 fe\cn 守护进程
/opt/start_fe.sh

开发环境升级过程中 ip 匹配关系
FE:xx.xx.xxx.xx     xx.xx.xxx.xx
BE:
xx.xx.xxx.xx       xx.xx.xxx.xx
xx.xx.xxx.xx      xx.xx.xxx.xx
xx.xx.xxx.xx     xx.xx.xxx.xx

复制以下目录到目标机
/data/starrocks/mirrorship/be*
/data/starrocks/mirrorship/fe*
/data/starrocks/fe/meta
/data/app/starrocks-manager-console/
/data/app/starrocks-manager
修改 /data/app/starrocks-manager/center/conf/center_service.conf 、 web.conf 需要修改备份库


## SR测试环境 xx.xx.xxx.xx
/etc/fstab 中添加 mount 开机挂载
xx.xx.xxx.xx:/starrocks /data/cfs nfs vers=3,nolock,proto=tcp,hard,timeo=600,retrans=2,_netdev,noresvport 0 0

元数据备份路径和脚本
mount -t nfs -o vers=4.0,noresvport xx.xx.xxx.xx:/starrocks /data/cfs; sudo mount -a
 生成每日备份脚本
echo '10 0 * * * root /data/cfs/sr_ddl_recovery/daily_backup.sh >> /data/cfs/sr_ddl_recovery/daily.log 2>&1 ' >> /etc/cron.d/sr_bak_daily
 生成小时备份脚本
echo '2 * * * * root /data/cfs/sr_ddl_recovery/hourly_backup.sh >> /data/cfs/sr_ddl_recovery/hourly.log 2>&1 ' >> /etc/cron.d/sr_bak_hourly
开源版本 fe\cn 守护进程
/opt/start_fe.sh

FE
xx.xx.xxx.xx    xx.xx.xxx.xx
BE
xx.xx.xxx.xx   xx.xx.xxx.xx
xx.xx.xxx.xx    xx.xx.xxx.xx
xx.xx.xxx.xx    xx.xx.xxx.xx


## 磁盘管理命令
查看云盘大小及名字如：/dev/vda
fdisk -l
卸载
umount /data2
修改磁盘自动挂载配置
vi /etc/fstab
重启配置
systemctl daemon-reload
