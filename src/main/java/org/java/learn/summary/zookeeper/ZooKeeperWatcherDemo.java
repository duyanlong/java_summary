package org.java.learn.summary.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * ZooKeeper监听器(Watcher)示例
 * 演示如何监听节点的数据变化和子节点变化
 */
public class ZooKeeperWatcherDemo implements Watcher {
    
    private static final String CONNECT_STRING = "localhost:2182";
    private static final int SESSION_TIMEOUT = 3000;
    private ZooKeeper zooKeeper;
    private CountDownLatch connectedSignal = new CountDownLatch(1);
    
    public void connect() throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(CONNECT_STRING, SESSION_TIMEOUT, this);
        connectedSignal.await();
        System.out.println("连接到ZooKeeper服务器成功");
    }
    
    /**
     * 监听节点数据变化
     * @param path 要监听的节点路径
     */
    public void watchNodeData(String path) {
        try {
            // 设置数据监听器
            byte[] data = zooKeeper.getData(path, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    System.out.println("=== 数据变化监听器触发 ===");
                    System.out.println("事件类型: " + event.getType());
                    System.out.println("节点路径: " + event.getPath());
                    System.out.println("连接状态: " + event.getState());
                    
                    if (event.getType() == Event.EventType.NodeDataChanged) {
                        System.out.println("节点数据发生变化，重新获取数据...");
                        try {
                            // 重新设置监听器（ZooKeeper的监听器是一次性的）
                            byte[] newData = zooKeeper.getData(path, this, null);
                            System.out.println("新数据: " + new String(newData));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }, null);
            
            System.out.println("开始监听节点 " + path + " 的数据变化");
            System.out.println("当前数据: " + new String(data));
            
        } catch (KeeperException | InterruptedException e) {
            System.err.println("设置数据监听器失败: " + e.getMessage());
        }
    }
    
    /**
     * 监听子节点变化
     * @param path 要监听的父节点路径
     */
    public void watchChildren(String path) {
        try {
            // 设置子节点监听器
            zooKeeper.getChildren(path, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    System.out.println("=== 子节点变化监听器触发 ===");
                    System.out.println("事件类型: " + event.getType());
                    System.out.println("节点路径: " + event.getPath());
                    
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                        System.out.println("子节点发生变化，重新获取子节点列表...");
                        try {
                            // 重新设置监听器
                            List<String> children = zooKeeper.getChildren(path, this);
                            System.out.println("当前子节点: " + children);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            
            System.out.println("开始监听节点 " + path + " 的子节点变化");
            
        } catch (KeeperException | InterruptedException e) {
            System.err.println("设置子节点监听器失败: " + e.getMessage());
        }
    }
    
    /**
     * 监听节点存在性变化
     * @param path 要监听的节点路径
     */
    public void watchNodeExists(String path) {
        try {
            // 设置存在性监听器
            Stat stat = zooKeeper.exists(path, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    System.out.println("=== 节点存在性监听器触发 ===");
                    System.out.println("事件类型: " + event.getType());
                    System.out.println("节点路径: " + event.getPath());
                    
                    if (event.getType() == Event.EventType.NodeCreated) {
                        System.out.println("节点被创建");
                    } else if (event.getType() == Event.EventType.NodeDeleted) {
                        System.out.println("节点被删除");
                    }
                    
                    // 重新设置监听器
                    try {
                        zooKeeper.exists(path, this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            
            if (stat != null) {
                System.out.println("节点 " + path + " 存在，开始监听其变化");
            } else {
                System.out.println("节点 " + path + " 不存在，监听其创建事件");
            }
            
        } catch (KeeperException | InterruptedException e) {
            System.err.println("设置存在性监听器失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建测试节点
     */
    public void createTestNode(String path, String data) {
        try {
            if (zooKeeper.exists(path, false) == null) {
                zooKeeper.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                System.out.println("创建测试节点: " + path);
            }
        } catch (Exception e) {
            System.err.println("创建测试节点失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新节点数据
     */
    public void updateNodeData(String path, String newData) {
        try {
            zooKeeper.setData(path, newData.getBytes(), -1);
            System.out.println("更新节点 " + path + " 数据为: " + newData);
        } catch (Exception e) {
            System.err.println("更新节点数据失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建子节点
     */
    public void createChildNode(String parentPath, String childName, String data) {
        try {
            String childPath = parentPath + "/" + childName;
            zooKeeper.create(childPath, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println("创建子节点: " + childPath);
        } catch (Exception e) {
            System.err.println("创建子节点失败: " + e.getMessage());
        }
    }
    
    public void close() throws InterruptedException {
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
    
    public static void main(String[] args) {
        ZooKeeperWatcherDemo demo = new ZooKeeperWatcherDemo();
        
        try {
            demo.connect();
            
            String testPath = "/watcher-test";
            String childTestPath = "/watcher-child-test";
            
            // 创建测试节点
            demo.createTestNode(testPath, "初始数据");
            demo.createTestNode(childTestPath, "父节点数据");
            
            // 设置各种监听器
            demo.watchNodeData(testPath);
            demo.watchChildren(childTestPath);
            demo.watchNodeExists("/non-existent");
            
            // 等待一段时间，让监听器生效
            Thread.sleep(1000);
            
            System.out.println("\n开始触发各种事件...\n");
            
            // 触发数据变化事件
            demo.updateNodeData(testPath, "第一次更新");
            Thread.sleep(1000);
            
            demo.updateNodeData(testPath, "第二次更新");
            Thread.sleep(1000);
            
            // 触发子节点变化事件
            demo.createChildNode(childTestPath, "child1", "子节点1数据");
            Thread.sleep(1000);
            
            demo.createChildNode(childTestPath, "child2", "子节点2数据");
            Thread.sleep(1000);
            
            // 触发节点创建事件
            demo.createTestNode("/non-existent", "新创建的节点");
            Thread.sleep(1000);
            
            System.out.println("\n监听器演示完成，程序将在5秒后退出...");
            Thread.sleep(5000);
            
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