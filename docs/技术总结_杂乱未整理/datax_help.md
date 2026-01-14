## datax 编译
mvn clean package -DskipTests assembly:assembly     
     
     python方式，暂时不考虑 ---
     python -j [jvm parameters]
     --jobid=[job unique id]
     -m [job runtime mode:standlone、local、distribute]
     -p [parameter  example: -p "-D partition=20210601"]
     -r [parameter used in view job config reader template]
     -w [parameter used in view job config writer template]
     -d ， --debug set to remoe debug mode
     --logLevel=[log level] set log level such as: debug.info .
     
     java方式，决定使用 ---
     java -server
     -Xms1g
     -Xmx1g
     -Xdebug
     -Xrunjdwp:transport=dt_socket,server=y,address=9999
     -Dloglevel=debug
     -XX:+HeapDumpOnOutOfMemoryError
     -XX:HeapDumpPath=/Users/yanlongdu/datax/log
     -Dloglevel=info
     -Dfile.encoding=UTF-8
     -Dlogback.statusListenerClass=ch.qos.logback.core.status.NopStatusListener
     -Djava.security.egd=file:///dev/urandom -Ddatax.home=/Users/yanlongdu/datax
     -Dlogback.configurationFile=/Users/yanlongdu/datax/conf/logback.xml
     -classpath /Users/yanlongdu/datax/lib/*:.
     -Dlog.file.name=le_xxxx_test_json com.alibaba.datax.core.Engine
     -mode standalone
     -jobid -1
     -job /Users/yanlongdu/datax/sample/xxxx_test.json
     -Dpartition=20210101