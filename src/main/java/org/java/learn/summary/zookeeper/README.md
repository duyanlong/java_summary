# ZooKeeper Java 使用示例

本目录包含了ZooKeeper各种特性的Java使用示例代码，连接到localhost:2181。

## 示例文件说明

### 1. ZooKeeperBasicOperations.java
**基本操作示例**
- 连接ZooKeeper服务器
- 创建节点（持久、临时、顺序节点）
- 读取节点数据
- 更新节点数据
- 删除节点
- 获取子节点列表
- 检查节点存在性

### 2. ZooKeeperWatcherDemo.java
**监听器(Watcher)示例**
- 监听节点数据变化
- 监听子节点变化
- 监听节点存在性变化
- 一次性监听器的重新设置
- 事件类型处理

### 3. ZooKeeperDistributedLock.java
**分布式锁实现**
- 基于临时顺序节点的分布式锁
- 公平锁机制
- 锁超时处理
- 多客户端竞争演示
- 自动释放机制

### 4. ZooKeeperConfigCenter.java
**配置中心实现**
- 配置项的增删改查
- 配置变更实时通知
- 本地配置缓存
- 配置监听器
- 动态配置更新

### 5. ZooKeeperServiceDiscovery.java
**服务发现实现**
- 服务注册与注销
- 服务实例发现
- 服务变更监听
- 负载均衡（随机策略）
- 健康检查机制

### 6. ZooKeeperACLDemo.java
**访问控制列表(ACL)示例**
- 开放ACL（无权限控制）
- 只读ACL
- Digest认证
- IP认证
- 复合ACL（多种权限组合）
- 权限检查演示

## 运行前准备

### 1. 启动ZooKeeper服务器
确保ZooKeeper服务器在localhost:2181上运行：

```bash
# 下载并启动ZooKeeper
# 方式1：使用Docker
docker run --name zookeeper -p 2181:2181 -d zookeeper:3.8

# 方式2：本地安装
# 下载ZooKeeper并解压
# 启动服务器
bin/zkServer.sh start
```

### 2. 编译项目
```bash
mvn clean compile
```

## 运行示例

### 运行单个示例
```bash
# 基本操作示例
mvn exec:java -Dexec.mainClass="org.java.learn.summary.zookeeper.ZooKeeperBasicOperations"

# 监听器示例
mvn exec:java -Dexec.mainClass="org.java.learn.summary.zookeeper.ZooKeeperWatcherDemo"

# 分布式锁示例
mvn exec:java -Dexec.mainClass="org.java.learn.summary.zookeeper.ZooKeeperDistributedLock"

# 配置中心示例
mvn exec:java -Dexec.mainClass="org.java.learn.summary.zookeeper.ZooKeeperConfigCenter"

# 服务发现示例
mvn exec:java -Dexec.mainClass="org.java.learn.summary.zookeeper.ZooKeeperServiceDiscovery"

# ACL权限控制示例
mvn exec:java -Dexec.mainClass="org.java.learn.summary.zookeeper.ZooKeeperACLDemo"
```

### 在IDE中运行
直接运行各个类的main方法即可。

## 核心概念说明

### 节点类型
- **PERSISTENT**: 持久节点，客户端断开连接后节点仍然存在
- **EPHEMERAL**: 临时节点，客户端断开连接后节点自动删除
- **PERSISTENT_SEQUENTIAL**: 持久顺序节点，节点名称后会自动添加序号
- **EPHEMERAL_SEQUENTIAL**: 临时顺序节点，临时节点+自动序号

### 监听器(Watcher)
- ZooKeeper的监听器是一次性的，触发后需要重新设置
- 支持监听数据变化、子节点变化、节点创建/删除等事件
- 监听器在客户端本地执行，不会阻塞服务器

### ACL权限
- **READ**: 读取节点数据和子节点列表
- **WRITE**: 修改节点数据
- **CREATE**: 创建子节点
- **DELETE**: 删除子节点
- **ADMIN**: 设置节点ACL权限

### 认证方式
- **digest**: 用户名密码认证
- **ip**: IP地址认证
- **world**: 开放访问
- **auth**: 已认证用户

## 注意事项

1. **连接管理**: 确保正确处理连接状态和异常
2. **会话超时**: 合理设置会话超时时间，避免频繁重连
3. **监听器重设**: 记住监听器是一次性的，需要在回调中重新设置
4. **权限控制**: 生产环境中务必设置适当的ACL权限
5. **异常处理**: 妥善处理各种ZooKeeper异常情况
6. **资源清理**: 使用完毕后及时关闭ZooKeeper连接

## 常见问题

### 1. 连接失败
- 检查ZooKeeper服务器是否启动
- 确认端口2181是否可访问
- 检查防火墙设置

### 2. 权限错误
- 确保设置了正确的认证信息
- 检查ACL权限配置
- 验证用户名密码是否正确

### 3. 节点已存在
- 在创建节点前先检查节点是否存在
- 使用适当的异常处理机制

### 4. 会话过期
- 增加会话超时时间
- 实现会话重连机制
- 监听会话状态变化

## 扩展学习

1. **集群配置**: 学习ZooKeeper集群的搭建和配置
2. **性能优化**: 了解ZooKeeper的性能特点和优化方法
3. **运维监控**: 掌握ZooKeeper的监控和运维技巧
4. **最佳实践**: 学习ZooKeeper在实际项目中的应用模式

## 参考资料

- [Apache ZooKeeper官方文档](https://zookeeper.apache.org/doc/current/)
- [ZooKeeper Java API文档](https://zookeeper.apache.org/doc/current/apidocs/zookeeper-server/index.html)
- [ZooKeeper最佳实践](https://cwiki.apache.org/confluence/display/ZOOKEEPER/ZooKeeperBestPractices)