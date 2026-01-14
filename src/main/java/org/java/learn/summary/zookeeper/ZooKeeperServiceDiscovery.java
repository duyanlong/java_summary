package org.java.learn.summary.zookeeper;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * 基于ZooKeeper的服务发现实现
 * 支持服务注册、发现、健康检查和负载均衡
 */
public class ZooKeeperServiceDiscovery implements Watcher {
    
    private static final String CONNECT_STRING = "localhost:2182";
    private static final int SESSION_TIMEOUT = 3000;
    private static final String SERVICE_ROOT_PATH = "/services";
    
    private ZooKeeper zooKeeper;
    private CountDownLatch connectedSignal = new CountDownLatch(1);
    
    /**
     * 服务实例信息
     */
    public static class ServiceInstance {
        private String serviceName;
        private String host;
        private int port;
        private String metadata;
        
        public ServiceInstance(String serviceName, String host, int port, String metadata) {
            this.serviceName = serviceName;
            this.host = host;
            this.port = port;
            this.metadata = metadata;
        }
        
        public String getAddress() {
            return host + ":" + port;
        }
        
        public String toJson() {
            return String.format("{\"host\":\"%s\",\"port\":%d,\"metadata\":\"%s\"}", 
                host, port, metadata);
        }
        
        public static ServiceInstance fromJson(String serviceName, String json) {
            // 简单的JSON解析（实际项目中建议使用JSON库）
            String host = extractValue(json, "host");
            int port = Integer.parseInt(extractValue(json, "port"));
            String metadata = extractValue(json, "metadata");
            return new ServiceInstance(serviceName, host, port, metadata);
        }
        
        private static String extractValue(String json, String key) {
            String pattern = "\"" + key + "\":";
            int start = json.indexOf(pattern) + pattern.length();
            if (json.charAt(start) == '"') {
                start++;
                int end = json.indexOf('"', start);
                return json.substring(start, end);
            } else {
                int end = json.indexOf(',', start);
                if (end == -1) end = json.indexOf('}', start);
                return json.substring(start, end);
            }
        }
        
        @Override
        public String toString() {
            return String.format("%s[%s:%d] - %s", serviceName, host, port, metadata);
        }
        
        // Getters
        public String getServiceName() { return serviceName; }
        public String getHost() { return host; }
        public int getPort() { return port; }
        public String getMetadata() { return metadata; }
    }
    
    /**
     * 服务变更监听器
     */
    public interface ServiceChangeListener {
        void onServiceAdded(ServiceInstance instance);
        void onServiceRemoved(ServiceInstance instance);
        void onServiceUpdated(ServiceInstance instance);
    }
    
    /**
     * 连接到ZooKeeper
     */
    public void connect() throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(CONNECT_STRING, SESSION_TIMEOUT, this);
        connectedSignal.await();
        
        // 确保服务根路径存在
        try {
            if (zooKeeper.exists(SERVICE_ROOT_PATH, false) == null) {
                zooKeeper.create(SERVICE_ROOT_PATH, new byte[0], 
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException e) {
            if (e.code() != KeeperException.Code.NODEEXISTS) {
                throw new RuntimeException("创建服务根路径失败", e);
            }
        }
        
        System.out.println("服务发现客户端连接成功");
    }
    
    /**
     * 注册服务实例
     * @param instance 服务实例
     * @return 注册的节点路径
     */
    public String registerService(ServiceInstance instance) {
        try {
            String servicePath = SERVICE_ROOT_PATH + "/" + instance.getServiceName();
            
            // 确保服务路径存在
            if (zooKeeper.exists(servicePath, false) == null) {
                zooKeeper.create(servicePath, new byte[0], 
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            
            // 创建临时顺序节点表示服务实例
            String instancePath = zooKeeper.create(
                servicePath + "/instance-",
                instance.toJson().getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL
            );
            
            System.out.println("服务注册成功: " + instance + " -> " + instancePath);
            return instancePath;
            
        } catch (Exception e) {
            System.err.println("服务注册失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 注销服务实例
     * @param instancePath 实例节点路径
     */
    public void unregisterService(String instancePath) {
        try {
            if (instancePath != null) {
                zooKeeper.delete(instancePath, -1);
                System.out.println("服务注销成功: " + instancePath);
            }
        } catch (Exception e) {
            System.err.println("服务注销失败: " + e.getMessage());
        }
    }
    
    /**
     * 发现服务实例
     * @param serviceName 服务名称
     * @return 服务实例列表
     */
    public List<ServiceInstance> discoverServices(String serviceName) {
        List<ServiceInstance> instances = new ArrayList<>();
        
        try {
            String servicePath = SERVICE_ROOT_PATH + "/" + serviceName;
            
            if (zooKeeper.exists(servicePath, false) == null) {
                System.out.println("服务不存在: " + serviceName);
                return instances;
            }
            
            List<String> children = zooKeeper.getChildren(servicePath, false);
            
            for (String child : children) {
                try {
                    String instancePath = servicePath + "/" + child;
                    byte[] data = zooKeeper.getData(instancePath, false, null);
                    
                    if (data != null) {
                        String json = new String(data);
                        ServiceInstance instance = ServiceInstance.fromJson(serviceName, json);
                        instances.add(instance);
                    }
                } catch (Exception e) {
                    System.err.println("解析服务实例失败: " + child + ", " + e.getMessage());
                }
            }
            
            System.out.println("发现服务实例 " + serviceName + ": " + instances.size() + " 个");
            
        } catch (Exception e) {
            System.err.println("服务发现失败: " + e.getMessage());
        }
        
        return instances;
    }
    
    /**
     * 监听服务变更
     * @param serviceName 服务名称
     * @param listener 变更监听器
     */
    public void watchService(String serviceName, ServiceChangeListener listener) {
        try {
            String servicePath = SERVICE_ROOT_PATH + "/" + serviceName;
            
            // 确保服务路径存在
            if (zooKeeper.exists(servicePath, false) == null) {
                zooKeeper.create(servicePath, new byte[0], 
                    ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            
            // 设置子节点监听器
            setServiceWatcher(servicePath, serviceName, listener);
            
            System.out.println("开始监听服务变更: " + serviceName);
            
        } catch (Exception e) {
            System.err.println("设置服务监听失败: " + e.getMessage());
        }
    }
    
    /**
     * 设置服务监听器
     */
    private void setServiceWatcher(String servicePath, String serviceName, ServiceChangeListener listener) {
        try {
            // 获取当前服务实例列表
            List<String> currentChildren = zooKeeper.getChildren(servicePath, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                        System.out.println("检测到服务变更: " + serviceName);
                        
                        // 重新设置监听器
                        setServiceWatcher(servicePath, serviceName, listener);
                        
                        // 处理服务变更（这里简化处理，实际应该比较新旧列表）
                        List<ServiceInstance> newInstances = discoverServices(serviceName);
                        for (ServiceInstance instance : newInstances) {
                            listener.onServiceUpdated(instance);
                        }
                    }
                }
            });
            
        } catch (Exception e) {
            System.err.println("设置服务监听器失败: " + e.getMessage());
        }
    }
    
    /**
     * 负载均衡 - 随机选择
     * @param serviceName 服务名称
     * @return 选中的服务实例
     */
    public ServiceInstance loadBalance(String serviceName) {
        List<ServiceInstance> instances = discoverServices(serviceName);
        
        if (instances.isEmpty()) {
            System.out.println("没有可用的服务实例: " + serviceName);
            return null;
        }
        
        // 简单的随机负载均衡
        Random random = new Random();
        ServiceInstance selected = instances.get(random.nextInt(instances.size()));
        
        System.out.println("负载均衡选择服务实例: " + selected);
        return selected;
    }
    
    /**
     * 获取所有服务列表
     */
    public void listAllServices() {
        try {
            List<String> services = zooKeeper.getChildren(SERVICE_ROOT_PATH, false);
            System.out.println("所有注册的服务:");
            
            for (String service : services) {
                List<ServiceInstance> instances = discoverServices(service);
                System.out.println("  " + service + " (" + instances.size() + " 个实例):");
                for (ServiceInstance instance : instances) {
                    System.out.println("    - " + instance.getAddress() + " [" + instance.getMetadata() + "]");
                }
            }
            
        } catch (Exception e) {
            System.err.println("获取服务列表失败: " + e.getMessage());
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
     * 服务发现使用示例
     */
    public static void main(String[] args) {
        ZooKeeperServiceDiscovery serviceDiscovery = new ZooKeeperServiceDiscovery();
        
        try {
            serviceDiscovery.connect();
            
            // 模拟注册多个服务实例
            System.out.println("=== 注册服务实例 ===");
            
            // 注册用户服务实例
            String userService1 = serviceDiscovery.registerService(
                new ServiceInstance("user-service", "192.168.1.10", 8080, "version=1.0,zone=A"));
            String userService2 = serviceDiscovery.registerService(
                new ServiceInstance("user-service", "192.168.1.11", 8080, "version=1.0,zone=B"));
            
            // 注册订单服务实例
            String orderService1 = serviceDiscovery.registerService(
                new ServiceInstance("order-service", "192.168.1.20", 8081, "version=1.1,zone=A"));
            String orderService2 = serviceDiscovery.registerService(
                new ServiceInstance("order-service", "192.168.1.21", 8081, "version=1.1,zone=B"));
            
            Thread.sleep(1000);
            
            // 服务发现
            System.out.println("\n=== 服务发现 ===");
            serviceDiscovery.listAllServices();
            
            // 负载均衡测试
            System.out.println("\n=== 负载均衡测试 ===");
            for (int i = 0; i < 5; i++) {
                ServiceInstance instance = serviceDiscovery.loadBalance("user-service");
                if (instance != null) {
                    System.out.println("第" + (i+1) + "次调用选择: " + instance.getAddress());
                }
            }
            
            // 设置服务监听
            System.out.println("\n=== 设置服务监听 ===");
            serviceDiscovery.watchService("user-service", new ServiceChangeListener() {
                @Override
                public void onServiceAdded(ServiceInstance instance) {
                    System.out.println(">>> 服务实例上线: " + instance);
                }
                
                @Override
                public void onServiceRemoved(ServiceInstance instance) {
                    System.out.println(">>> 服务实例下线: " + instance);
                }
                
                @Override
                public void onServiceUpdated(ServiceInstance instance) {
                    System.out.println(">>> 服务实例更新: " + instance);
                }
            });
            
            Thread.sleep(2000);
            
            // 模拟服务下线
            System.out.println("\n=== 模拟服务下线 ===");
            serviceDiscovery.unregisterService(userService1);
            
            Thread.sleep(1000);
            
            // 再次查看服务列表
            System.out.println("\n=== 服务下线后的服务列表 ===");
            serviceDiscovery.listAllServices();
            
            // 再次进行负载均衡
            System.out.println("\n=== 服务下线后的负载均衡 ===");
            for (int i = 0; i < 3; i++) {
                ServiceInstance instance = serviceDiscovery.loadBalance("user-service");
                if (instance != null) {
                    System.out.println("调用选择: " + instance.getAddress());
                }
            }
            
            System.out.println("\n服务发现演示完成，程序将在5秒后退出...");
            Thread.sleep(5000);
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                serviceDiscovery.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}