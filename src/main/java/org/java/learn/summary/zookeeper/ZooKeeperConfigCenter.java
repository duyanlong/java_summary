package org.java.learn.summary.zookeeper;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * 基于ZooKeeper的配置中心实现
 * 支持配置的动态更新和实时通知
 */
public class ZooKeeperConfigCenter implements Watcher {
    
    private static final String CONNECT_STRING = "localhost:2182";
    private static final int SESSION_TIMEOUT = 3000;
    private static final String CONFIG_ROOT_PATH = "/config-center";
    
    private ZooKeeper zooKeeper;
    private CountDownLatch connectedSignal = new CountDownLatch(1);
    
    // 本地配置缓存
    private Map<String, String> configCache = new ConcurrentHashMap<>();
    
    // 配置变更监听器
    private Map<String, ConfigChangeListener> listeners = new ConcurrentHashMap<>();
    
    /**
     * 配置变更监听器接口
     */
    public interface ConfigChangeListener {
        void onConfigChanged(String key, String oldValue, String newValue);
    }
    
    /**
     * 连接到ZooKeeper
     */
    public void connect() throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(CONNECT_STRING, SESSION_TIMEOUT, this);
        connectedSignal.await();
        
        // 确保配置根路径存在
        try {
            if (zooKeeper.exists(CONFIG_ROOT_PATH, false) == null) {
                zooKeeper.create(CONFIG_ROOT_PATH, new byte[0], 
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException e) {
            if (e.code() != KeeperException.Code.NODEEXISTS) {
                throw new RuntimeException("创建配置根路径失败", e);
            }
        }
        
        System.out.println("配置中心连接成功");
    }
    
    /**
     * 设置配置项
     * @param key 配置键
     * @param value 配置值
     */
    public void setConfig(String key, String value) {
        try {
            String configPath = CONFIG_ROOT_PATH + "/" + key;
            
            // 检查节点是否存在
            if (zooKeeper.exists(configPath, false) == null) {
                // 创建新的配置节点
                zooKeeper.create(configPath, value.getBytes(), 
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                System.out.println("创建配置项: " + key + " = " + value);
            } else {
                // 更新现有配置
                zooKeeper.setData(configPath, value.getBytes(), -1);
                System.out.println("更新配置项: " + key + " = " + value);
            }
            
            // 更新本地缓存
            String oldValue = configCache.put(key, value);
            
            // 触发监听器
            ConfigChangeListener listener = listeners.get(key);
            if (listener != null) {
                listener.onConfigChanged(key, oldValue, value);
            }
            
        } catch (Exception e) {
            System.err.println("设置配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取配置项
     * @param key 配置键
     * @return 配置值
     */
    public String getConfig(String key) {
        // 先从缓存获取
        String cachedValue = configCache.get(key);
        if (cachedValue != null) {
            return cachedValue;
        }
        
        // 从ZooKeeper获取
        try {
            String configPath = CONFIG_ROOT_PATH + "/" + key;
            byte[] data = zooKeeper.getData(configPath, false, null);
            
            if (data != null) {
                String value = new String(data);
                configCache.put(key, value);
                return value;
            }
        } catch (KeeperException.NoNodeException e) {
            System.out.println("配置项不存在: " + key);
        } catch (Exception e) {
            System.err.println("获取配置失败: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 监听配置变更
     * @param key 配置键
     * @param listener 变更监听器
     */
    public void watchConfig(String key, ConfigChangeListener listener) {
        try {
            String configPath = CONFIG_ROOT_PATH + "/" + key;
            
            // 注册监听器
            listeners.put(key, listener);
            
            // 设置ZooKeeper监听器
            setConfigWatcher(configPath, key);
            
            // 初始化缓存
            String initialValue = getConfig(key);
            if (initialValue != null) {
                configCache.put(key, initialValue);
            }
            
            System.out.println("开始监听配置项: " + key);
            
        } catch (Exception e) {
            System.err.println("设置配置监听失败: " + e.getMessage());
        }
    }
    
    /**
     * 设置ZooKeeper配置监听器
     */
    private void setConfigWatcher(String configPath, String key) {
        try {
            zooKeeper.getData(configPath, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeDataChanged) {
                        System.out.println("检测到配置变更: " + key);
                        
                        try {
                            // 获取新的配置值
                            byte[] newData = zooKeeper.getData(configPath, this, null);
                            String newValue = new String(newData);
                            
                            // 获取旧值
                            String oldValue = configCache.get(key);
                            
                            // 更新缓存
                            configCache.put(key, newValue);
                            
                            // 触发监听器
                            ConfigChangeListener listener = listeners.get(key);
                            if (listener != null) {
                                listener.onConfigChanged(key, oldValue, newValue);
                            }
                            
                        } catch (Exception e) {
                            System.err.println("处理配置变更失败: " + e.getMessage());
                        }
                    } else if (event.getType() == Event.EventType.NodeCreated) {
                        System.out.println("配置项被创建: " + key);
                        // 重新设置监听器
                        setConfigWatcher(configPath, key);
                    }
                }
            }, null);
            
        } catch (KeeperException.NoNodeException e) {
            // 节点不存在，监听节点创建事件
            try {
                zooKeeper.exists(configPath, new Watcher() {
                    @Override
                    public void process(WatchedEvent event) {
                        if (event.getType() == Event.EventType.NodeCreated) {
                            System.out.println("配置节点被创建: " + key);
                            setConfigWatcher(configPath, key);
                        }
                    }
                });
            } catch (Exception ex) {
                System.err.println("设置节点创建监听失败: " + ex.getMessage());
            }
        } catch (Exception e) {
            System.err.println("设置配置监听器失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除配置项
     * @param key 配置键
     */
    public void deleteConfig(String key) {
        try {
            String configPath = CONFIG_ROOT_PATH + "/" + key;
            zooKeeper.delete(configPath, -1);
            
            // 从缓存中移除
            String oldValue = configCache.remove(key);
            
            // 触发监听器
            ConfigChangeListener listener = listeners.get(key);
            if (listener != null) {
                listener.onConfigChanged(key, oldValue, null);
            }
            
            System.out.println("删除配置项: " + key);
            
        } catch (Exception e) {
            System.err.println("删除配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有配置项
     */
    public void listAllConfigs() {
        try {
            List<String> children = zooKeeper.getChildren(CONFIG_ROOT_PATH, false);
            System.out.println("所有配置项:");
            
            for (String child : children) {
                String value = getConfig(child);
                System.out.println("  " + child + " = " + value);
            }
            
        } catch (Exception e) {
            System.err.println("获取配置列表失败: " + e.getMessage());
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
    
    /**
     * 配置中心使用示例
     */
    public static void main(String[] args) {
        ZooKeeperConfigCenter configCenter = new ZooKeeperConfigCenter();
        
        try {
            configCenter.connect();
            
            // 设置一些初始配置
            configCenter.setConfig("database.url", "jdbc:mysql://localhost:3306/test");
            configCenter.setConfig("database.username", "root");
            configCenter.setConfig("database.password", "password");
            configCenter.setConfig("app.name", "MyApplication");
            configCenter.setConfig("app.version", "1.0.0");
            
            // 监听配置变更
            configCenter.watchConfig("database.url", new ZooKeeperConfigCenter.ConfigChangeListener() {
                @Override
                public void onConfigChanged(String key, String oldValue, String newValue) {
                    System.out.println(">>> 配置变更通知: " + key);
                    System.out.println("    旧值: " + oldValue);
                    System.out.println("    新值: " + newValue);
                    System.out.println("    应用需要重新连接数据库!");
                }
            });
            
            configCenter.watchConfig("app.version", (key, oldValue, newValue) -> {
                System.out.println(">>> 应用版本更新: " + oldValue + " -> " + newValue);
            });
            
            // 显示所有配置
            System.out.println("\n=== 当前所有配置 ===");
            configCenter.listAllConfigs();
            
            // 等待一段时间
            Thread.sleep(2000);
            
            System.out.println("\n=== 开始测试配置动态更新 ===");
            
            // 模拟配置更新
            configCenter.setConfig("database.url", "jdbc:mysql://newhost:3306/newdb");
            Thread.sleep(1000);
            
            configCenter.setConfig("app.version", "1.0.1");
            Thread.sleep(1000);
            
            configCenter.setConfig("database.password", "newpassword");
            Thread.sleep(1000);
            
            // 测试获取配置
            System.out.println("\n=== 测试配置获取 ===");
            System.out.println("数据库URL: " + configCenter.getConfig("database.url"));
            System.out.println("应用版本: " + configCenter.getConfig("app.version"));
            
            System.out.println("\n配置中心演示完成，程序将在5秒后退出...");
            Thread.sleep(5000);
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                configCenter.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}