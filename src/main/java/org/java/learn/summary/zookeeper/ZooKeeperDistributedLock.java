package org.java.learn.summary.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 基于ZooKeeper的分布式锁实现
 * 使用临时顺序节点实现公平锁机制
 */
public class ZooKeeperDistributedLock implements Watcher {
    
    private static final String CONNECT_STRING = "localhost:2182";
    private static final int SESSION_TIMEOUT = 3000;
    private static final String LOCK_ROOT_PATH = "/distributed-locks";
    
    private ZooKeeper zooKeeper;
    private CountDownLatch connectedSignal = new CountDownLatch(1);
    private String lockPath;
    private String currentLockPath;
    private CountDownLatch lockAcquiredSignal;
    
    public ZooKeeperDistributedLock(String lockPath) {
        this.lockPath = LOCK_ROOT_PATH + "/" + lockPath;
    }
    
    /**
     * 连接到ZooKeeper
     */
    public void connect() throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(CONNECT_STRING, SESSION_TIMEOUT, this);
        connectedSignal.await();
        
        // 确保锁根路径存在
        try {
            if (zooKeeper.exists(LOCK_ROOT_PATH, false) == null) {
                zooKeeper.create(LOCK_ROOT_PATH, new byte[0], 
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException e) {
            // 如果节点已存在，忽略异常
            if (e.code() != KeeperException.Code.NODEEXISTS) {
                throw new RuntimeException("创建锁根路径失败", e);
            }
        }
        
        System.out.println("成功连接到ZooKeeper服务器");
    }
    
    /**
     * 获取分布式锁
     * @param timeout 超时时间（毫秒）
     * @return 是否成功获取锁
     */
    public boolean acquireLock(long timeout) {
        try {
            // 创建临时顺序节点
            currentLockPath = zooKeeper.create(
                lockPath + "/lock-", 
                new byte[0], 
                ZooDefs.Ids.OPEN_ACL_UNSAFE, 
                CreateMode.EPHEMERAL_SEQUENTIAL
            );
            
            System.out.println("创建锁节点: " + currentLockPath);
            
            // 尝试获取锁
            return tryAcquireLock(timeout);
            
        } catch (Exception e) {
            System.err.println("获取锁失败: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 尝试获取锁的核心逻辑
     */
    private boolean tryAcquireLock(long timeout) throws KeeperException, InterruptedException {
        while (true) {
            // 获取所有锁节点并排序
            List<String> lockNodes = zooKeeper.getChildren(lockPath, false);
            Collections.sort(lockNodes);
            
            // 获取当前节点的序号
            String currentNodeName = currentLockPath.substring(currentLockPath.lastIndexOf("/") + 1);
            int currentIndex = lockNodes.indexOf(currentNodeName);
            
            if (currentIndex == -1) {
                throw new RuntimeException("当前锁节点不存在于子节点列表中");
            }
            
            // 如果是最小的节点，则获得锁
            if (currentIndex == 0) {
                System.out.println("成功获取分布式锁: " + currentLockPath);
                return true;
            }
            
            // 否则监听前一个节点
            String prevNodeName = lockNodes.get(currentIndex - 1);
            String prevNodePath = lockPath + "/" + prevNodeName;
            
            lockAcquiredSignal = new CountDownLatch(1);
            
            // 监听前一个节点的删除事件
            Stat stat = zooKeeper.exists(prevNodePath, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeDeleted) {
                        System.out.println("前置节点被删除，尝试获取锁: " + event.getPath());
                        lockAcquiredSignal.countDown();
                    }
                }
            });
            
            // 如果前一个节点已经不存在，重新尝试
            if (stat == null) {
                continue;
            }
            
            System.out.println("等待前置节点释放锁: " + prevNodePath);
            
            // 等待前一个节点释放锁或超时
            if (timeout > 0) {
                boolean acquired = lockAcquiredSignal.await(timeout, TimeUnit.MILLISECONDS);
                if (!acquired) {
                    System.out.println("获取锁超时");
                    return false;
                }
            } else {
                lockAcquiredSignal.await();
            }
        }
    }
    
    /**
     * 释放分布式锁
     */
    public void releaseLock() {
        try {
            if (currentLockPath != null) {
                zooKeeper.delete(currentLockPath, -1);
                System.out.println("成功释放分布式锁: " + currentLockPath);
                currentLockPath = null;
            }
        } catch (Exception e) {
            System.err.println("释放锁失败: " + e.getMessage());
        }
    }
    
    /**
     * 关闭连接
     */
    public void close() throws InterruptedException {
        releaseLock();
        if (zooKeeper != null) {
            zooKeeper.close();
        }
    }
    
    @Override
    public void process(WatchedEvent event) {
        if (event.getState() == Event.KeeperState.SyncConnected) {
            connectedSignal.countDown();
        }
    }
    
    /**
     * 分布式锁使用示例
     */
    public static void main(String[] args) {
        // 模拟多个客户端竞争同一个锁
        for (int i = 0; i < 3; i++) {
            final int clientId = i + 1;
            
            new Thread(() -> {
                ZooKeeperDistributedLock lock = new ZooKeeperDistributedLock("test-lock");
                
                try {
                    System.out.println("客户端 " + clientId + " 启动");
                    lock.connect();
                    
                    // 尝试获取锁
                    System.out.println("客户端 " + clientId + " 尝试获取锁...");
                    boolean acquired = lock.acquireLock(10000); // 10秒超时
                    
                    if (acquired) {
                        System.out.println("客户端 " + clientId + " 获得锁，开始执行业务逻辑...");
                        
                        // 模拟业务处理
                        Thread.sleep(2000);
                        
                        System.out.println("客户端 " + clientId + " 业务逻辑执行完成");
                    } else {
                        System.out.println("客户端 " + clientId + " 获取锁失败");
                    }
                    
                } catch (Exception e) {
                    System.err.println("客户端 " + clientId + " 执行异常: " + e.getMessage());
                } finally {
                    try {
                        lock.close();
                        System.out.println("客户端 " + clientId + " 关闭连接");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, "Client-" + clientId).start();
            
            // 稍微错开启动时间
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}