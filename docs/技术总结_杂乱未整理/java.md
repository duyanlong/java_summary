## 查询目录下哪些 jar 中有对应类
find . -name "*.jar" -exec grep -Hls "Multimaps" {} \;

find . -name "*.jar" -exec grep -Hls "org.owasp.encoder.Encode" {} \;
find . -name "*.jar" -exec grep -Hls "org.apache.zookeeper.KeeperException" {} \;
find . -name "*.jar" -exec grep -Hls "org.apache.zookeeper.server.quorum.flexible.QuorumMaj" {} \;

## mac 下运行 jd-gui 反编译工具
cd /Users/yanlongdu/jd-gui-osx-1.6.6/JD-GUI.app/Contents/Resources/Java
source ~/.bash_profile
java -jar jd-gui-1.6.6-min.jar

## 查看 java堆栈、内存
jdk 11.0*
jhsdb jmap --heap --pid 24167
低版本
jmap -heap 24167 查看内存使用统计信息
jmap -histo 24167 查看jvm 中常量及变量实例分布情况

jstack  -m -l 24167 查看 jvm 中线程及锁信息

xx.xx.xxx.xx
jmap -heap 14787 >> /data/sr_jvm_data/20240517_jmap.txt
jmap -histo 14787 >> /data/sr_jvm_data/20240517_jmap.txt
jstack  -m -l 14787 > /data/sr_jvm_data/20240517_jstack.txt

jinfo 14787  查看jvm参数

* 挂载云硬盘
fdisk -l
mkfs -t ext4 /dev/vdb
mkdir /data
mount /dev/vdb /data
df -TH

* 配置机器重启后会自动挂载:
1、查看弹性云硬盘的软链接
ls -l /dev/disk/by-id
2、添加自动挂载信息
echo '/dev/disk/by-id/virtio-disk-ap0mxzpm /data ext4 defaults,nofail 0 0' >> /etc/fstab
cat /etc/fstab

3、检查是否成功，运行不报错就说明成功
mount -a 


it-starrocks-volume--dev-1253428821
存储桶的名称和secretid
starrocks-dev-default 	xx
starrocks-dev-hr 	xx
starrocks-dev-fin 	xxx

secretkey
xx
xx
xxx

