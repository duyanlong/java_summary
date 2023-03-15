### kafka
#### kafka为什么读写速度快
java对象如果存在内存中，随着数据的增多java的GC时间会变长、内存占用大使内存成为数据存储的一个很大的限制，而且是非持久的，重启后数据会丢失；
##### 顺序读写
kafka决定用顺序读写磁盘方式（read-ahead、write-behind），读写速度超过内存随机读写，同时硬盘资源好扩展，系统冷启动的话磁盘缓存数据依然可用；
数据写时write-behind，读时根据offset read-ahead； 

##### MMFile（memofy mapped Files）
即便顺序读写磁盘的访问速度也赶不上内存，所以kafka的数据并不是实时写入硬盘，它充分利用现代操作系统分页存储利用内存来提高io效率，
memory mapped files它直接利用操作系统的page来实现文件到物理内存的映射，完成映射后对物理内存的操作会被同步到硬盘上；
省去用户内存空间和内核内存空间复制的开销，调用文件的read会把数据先放到内核的内存中，然后再复制到用户空间的内存中，但会不可靠，写到mmap中的数据并没有被真正写到硬盘，操作系统会在程序主动调用flush时才把数据刷新到磁盘，kafka提供每写一条数据自动flush（同步）和根据通过时间、条数定期flush（异步）两种方式；

##### 基于sendfile实现zero copy
传统文件io读写的流程是：磁盘->内核buffer->用户buffer->socket缓冲区->协议引擎（网卡）
而相对于传统方式2.1系统版本后，减少了内核缓冲区到用户buffer，再由用户buffer到socket相关缓冲的copy，因此新的流程进行了简化效率会快很多，新流程如下：
磁盘->内核buffer->socket缓冲区->协议引擎（网卡）

#### kafka 优化
##### partition数量配置
kafka topic对应的partitions数量直接决定topic的并发能力，因此在合适的情况下增加partitions数量可以提高并发能力

##### 日志保留策略设置
当kafka broker写入数据量比较大时，会生成很多数据文件，占用大量磁盘空间，kafka启动时会扫描加载这些文件，如果文件太多会影响启动，kafka文件默认保留7天，可以根据具体需要调整保留时间；

##### 文件刷盘策略
为了提高producer写入吞吐量，需要定期批量将memory mappered file刷新到文件，可以通过下面配置优化：
```text
每当producer写入10000条消息时，刷数据到磁盘
log.flush.interval.messages=10000

每间隔1秒钟时间，刷数据到磁盘
log.flush.interval.ms=1000
```
##### 批量压缩
如果每条数据进行压缩，这样解压缩过程会耗费很长时间，但是将数据分批进行解压缩效率会高很多，同时基于压缩后的数据进行网络io传输，数据量变小后传输速度回快很多；

##### 网络和io操作线程配置优化
一般num.network.threads主要处理网络io，读写缓冲区数据，基本没有io等待，配置线程数量为cpu核数加1。
```
broker处理消息的最大线程数
num.network.threads=xxx
```

num.io.threads主要进行磁盘io操作，高峰期可能有些io等待，因此配置需要大些。配置线程数量为cpu核数2倍，最大不超过3倍。
```
broker处理磁盘IO的线程数
num.io.threads=xxx
```
加入队列的最大请求数,超过该值，network thread阻塞  queued.max.requests=5000
server使用的send buffer大小  socket.send.buffer.bytes=1024000
server使用的recive buffer大小。 socket.receive.buffer.bytes=1024000

##### 异步提交 kafka.javaapi.producer
```text
request.required.acks=0  
producer.type=async     
##在异步模式下，一个batch发送的消息数量。producer会等待直到要发送的消息数量达到这个值，之后才会发送。但如果消息数量不够，达到queue.buffer.max.ms时也会直接发送。       
batch.num.messages=100  
##默认值：200，当使用异步模式时，缓冲数据的最大时间。例如设为100的话，会每隔100毫秒把所有的消息批量发送。这会提高吞吐量，但是会增加消息的到达延时
queue.buffering.max.ms=100  
##默认值：5000，在异步模式下，producer端允许buffer的最大消息数量，如果producer无法尽快将消息发送给broker，从而导致消息在producer端大量沉积，如果消息的条数达到此配置值，将会导致producer端阻塞或者消息被抛弃。
queue.buffering.max.messages=1000 ##发送队列缓冲长度
##默认值：10000，当消息在producer端沉积的条数达到 queue.buffering.max.meesages 时，阻塞一定时间后，队列仍然没有enqueue(producer仍然没有发送出任何消息)。此时producer可以继续阻塞或者将消息抛弃，此timeout值用于控制阻塞的时间，如果值为-1（默认值）则 无阻塞超时限制，消息不会被抛弃；如果值为0 则立即清空队列，消息被抛弃。
queue.enqueue.timeout.ms=100     
compression.codec=gzip
```

#### kafka与传统消息队列的区别
1、首先kafka是消息分区(partition)，每个topic的数据分布在多个分区中，这样topic的数据就不再受单台服务器存储空间大小的限制，另外消息的处理可以在多个服务器上并行，提高并行度；
2、为了保证高可用，每个partition的数据存在多个副本(replica)，这样如果部分副本所在服务器挂掉之后不影响数据的使用，保证数据的持续高可用性；
3、kafka可以保证分区内部消息消费的有序性；
4、kafka还具有concumer group的概念，每个分区可以被同一group的一个consumer消费，但可以被多个group消费；
5、kafka数据是顺序读写磁盘的，因此他的读写速度比较快，同时注定他是持久化的，宕机不会丢失数据；
