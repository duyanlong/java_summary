package org.java.learn.summary.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * ZooKeeper基本操作示例
 * 演示连接、创建节点、读取数据、更新数据、删除节点等基本功能
 */
public class ZooKeeperBasicOperations implements Watcher {
    
    private static final String CONNECT_STRING = "localhost:2182";
    private static final int SESSION_TIMEOUT = 30000;
    private ZooKeeper zooKeeper;
    private CountDownLatch connectedSignal = new CountDownLatch(1);
    
    /**
     * 连接到ZooKeeper服务器
     */
    public void connect() throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(CONNECT_STRING, SESSION_TIMEOUT, this);
        // 等待连接建立
        connectedSignal.await();
        System.out.println("已成功连接到ZooKeeper服务器: " + CONNECT_STRING);
    }
    
    /**
     * 关闭ZooKeeper连接
     */
    public void close() throws InterruptedException {
        if (zooKeeper != null) {
            zooKeeper.close();
            System.out.println("ZooKeeper连接已关闭");
        }
    }
    
    /**
     * 创建节点
     * @param path 节点路径
     * @param data 节点数据
     * @param createMode 创建模式
     */
    public void createNode(String path, String data, CreateMode createMode) {
        try {
            String actualPath = zooKeeper.create(path, data.getBytes(), 
                ZooDefs.Ids.OPEN_ACL_UNSAFE, createMode);
            System.out.println("成功创建节点: " + actualPath + ", 数据: " + data);
        } catch (KeeperException | InterruptedException e) {
            System.err.println("创建节点失败: " + e.getMessage());
        }
    }
    
    /**
     * 读取节点数据
     * @param path 节点路径
     * @return 节点数据
     */
    public String getData(String path) {
        try {
            Stat stat = new Stat();
            byte[] data = zooKeeper.getData(path, false, stat);
            String dataStr = new String(data);
            System.out.println("读取节点 " + path + " 数据: " + dataStr);
            System.out.println("节点版本: " + stat.getVersion() + ", 创建时间: " + stat.getCtime());
            return dataStr;
        } catch (KeeperException | InterruptedException e) {
            System.err.println("读取节点数据失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 更新节点数据
     * @param path 节点路径
     * @param newData 新数据
     */
    public void setData(String path, String newData) {
        try {
            Stat stat = zooKeeper.setData(path, newData.getBytes(), -1);
            System.out.println("成功更新节点 " + path + " 数据: " + newData);
            System.out.println("新版本号: " + stat.getVersion());
        } catch (KeeperException | InterruptedException e) {
            System.err.println("更新节点数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除节点
     * @param path 节点路径
     */
    public void deleteNode(String path) {
        try {
            zooKeeper.delete(path, -1);
            System.out.println("成功删除节点: " + path);
        } catch (KeeperException | InterruptedException e) {
            System.err.println("删除节点失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取子节点列表
     * @param path 父节点路径
     */
    public void getChildren(String path) {
        try {
            List<String> children = zooKeeper.getChildren(path, false);
            System.out.println("节点 " + path + " 的子节点:");
            for (String child : children) {
                System.out.println("  - " + child);
            }
        } catch (KeeperException | InterruptedException e) {
            System.err.println("获取子节点失败: " + e.getMessage());
        }
    }
    
    /**
     * 检查节点是否存在
     * @param path 节点路径
     */
    public void exists(String path) {
        try {
            Stat stat = zooKeeper.exists(path, false);
            if (stat != null) {
                System.out.println("节点 " + path + " 存在, 版本: " + stat.getVersion());
            } else {
                System.out.println("节点 " + path + " 不存在");
            }
        } catch (KeeperException | InterruptedException e) {
            System.err.println("检查节点存在性失败: " + e.getMessage());
        }
    }
    
    @Override
    public void process(WatchedEvent event) {
        if (event.getState() == Event.KeeperState.SyncConnected) {
            connectedSignal.countDown();
        }
        System.out.println("收到事件: " + event.getType() + ", 路径: " + event.getPath());
    }
    
    /**
     * 演示基本操作
     */
    public static void main(String[] args) {
        ZooKeeperBasicOperations demo = new ZooKeeperBasicOperations();
        
        try {
            // 连接ZooKeeper
            demo.connect();
            
            // 创建持久节点
            demo.createNode("/test", "初始数据", CreateMode.PERSISTENT);
            
            // 创建临时节点
            demo.createNode("/temp", "临时数据", CreateMode.EPHEMERAL);
            
            // 创建持久顺序节点
            demo.createNode("/seq", "顺序数据", CreateMode.PERSISTENT_SEQUENTIAL);
            
            // 读取数据
            demo.getData("/test");
            
            // 更新数据
            demo.setData("/test", "更新后的数据");
            
            // 再次读取数据
            demo.getData("/test");
            
            // 检查节点存在性
            demo.exists("/test");
            demo.exists("/nonexistent");
            
            // 获取根节点的子节点
            demo.getChildren("/");
            
            // 删除节点
            demo.deleteNode("/test");
            
            // 验证删除
            demo.exists("/test");
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                demo.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}