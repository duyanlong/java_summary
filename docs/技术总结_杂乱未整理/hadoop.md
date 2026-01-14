快速结束namenode合并editlog
su hadoop
hdfs dfsadmin -safemode enter
hdfs dfsadmin -saveNamespace
hdfs dfsadmin -safemode leave


# 重置/目录下所有文件副本数
hadoop fs -setrep -R 1 / 