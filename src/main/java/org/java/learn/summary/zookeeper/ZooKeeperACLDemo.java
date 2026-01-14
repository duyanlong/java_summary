package org.java.learn.summary.zookeeper;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * ZooKeeper访问控制列表(ACL)示例
 * 演示权限控制、用户认证和安全访问
 */
public class ZooKeeperACLDemo implements Watcher {
    
    private static final String CONNECT_STRING = "localhost:2181";
    private static final int SESSION_TIMEOUT = 3000;
    
    private ZooKeeper zooKeeper;
    private CountDownLatch connectedSignal = new CountDownLatch(1);
    
    /**
     * 连接到ZooKeeper
     */
    public void connect() throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(CONNECT_STRING, SESSION_TIMEOUT, this);
        connectedSignal.await();
        System.out.println("连接到ZooKeeper服务器成功");
    }
    
    /**
     * 带认证信息连接
     */
    public void connectWithAuth(String scheme, String auth) throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(CONNECT_STRING, SESSION_TIMEOUT, this);
        connectedSignal.await();
        
        // 添加认证信息
        zooKeeper.addAuthInfo(scheme, auth.getBytes());
        System.out.println("带认证信息连接成功: " + scheme + ":" + auth);
    }
    
    /**
     * 演示开放ACL（无权限控制）
     */
    public void demonstrateOpenACL() {
        try {
            String path = "/open-node";
            
            // 使用开放ACL创建节点
            zooKeeper.create(path, "开放访问数据".getBytes(), 
                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            
            System.out.println("创建开放ACL节点: " + path);
            
            // 任何人都可以读取
            byte[] data = zooKeeper.getData(path, false, null);
            System.out.println("读取数据: " + new String(data));
            
            // 任何人都可以修改
            zooKeeper.setData(path, "修改后的数据".getBytes(), -1);
            System.out.println("修改数据成功");
            
            // 查看ACL信息
            List<ACL> acls = zooKeeper.getACL(path, null);
            System.out.println("节点ACL信息:");
            printACLs(acls);
            
        } catch (Exception e) {
            System.err.println("开放ACL演示失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示只读ACL
     */
    public void demonstrateReadOnlyACL() {
        try {
            String path = "/readonly-node";
            
            // 创建只读ACL
            List<ACL> readOnlyACL = new ArrayList<>();
            readOnlyACL.add(new ACL(ZooDefs.Perms.READ, ZooDefs.Ids.ANYONE_ID_UNSAFE));
            
            zooKeeper.create(path, "只读数据".getBytes(), readOnlyACL, CreateMode.PERSISTENT);
            System.out.println("创建只读ACL节点: " + path);
            
            // 可以读取
            byte[] data = zooKeeper.getData(path, false, null);
            System.out.println("读取数据: " + new String(data));
            
            // 尝试修改（应该失败）
            try {
                zooKeeper.setData(path, "尝试修改".getBytes(), -1);
                System.out.println("修改成功（不应该发生）");
            } catch (KeeperException.NoAuthException e) {
                System.out.println("修改失败，权限不足: " + e.getMessage());
            }
            
            // 查看ACL信息
            List<ACL> acls = zooKeeper.getACL(path, null);
            System.out.println("只读节点ACL信息:");
            printACLs(acls);
            
        } catch (Exception e) {
            System.err.println("只读ACL演示失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示Digest认证
     */
    public void demonstrateDigestAuth() {
        try {
            String username = "admin";
            String password = "secret";
            String auth = username + ":" + password;
            
            // 生成digest
            String digest = DigestAuthenticationProvider.generateDigest(auth);
            System.out.println("生成的digest: " + digest);
            
            // 添加认证信息
            zooKeeper.addAuthInfo("digest", auth.getBytes());
            
            String path = "/digest-node";
            
            // 创建需要digest认证的ACL
            List<ACL> digestACL = new ArrayList<>();
            digestACL.add(new ACL(ZooDefs.Perms.ALL, new Id("digest", digest)));
            
            zooKeeper.create(path, "需要认证的数据".getBytes(), digestACL, CreateMode.PERSISTENT);
            System.out.println("创建digest认证节点: " + path);
            
            // 认证用户可以访问
            byte[] data = zooKeeper.getData(path, false, null);
            System.out.println("认证用户读取数据: " + new String(data));
            
            // 认证用户可以修改
            zooKeeper.setData(path, "认证用户修改的数据".getBytes(), -1);
            System.out.println("认证用户修改数据成功");
            
            // 查看ACL信息
            List<ACL> acls = zooKeeper.getACL(path, null);
            System.out.println("Digest认证节点ACL信息:");
            printACLs(acls);
            
        } catch (Exception e) {
            System.err.println("Digest认证演示失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示IP认证
     */
    public void demonstrateIPAuth() {
        try {
            String path = "/ip-node";
            String allowedIP = "127.0.0.1"; // 只允许本地访问
            
            // 创建IP认证ACL
            List<ACL> ipACL = new ArrayList<>();
            ipACL.add(new ACL(ZooDefs.Perms.ALL, new Id("ip", allowedIP)));
            
            zooKeeper.create(path, "IP限制数据".getBytes(), ipACL, CreateMode.PERSISTENT);
            System.out.println("创建IP认证节点: " + path + " (只允许 " + allowedIP + " 访问)");
            
            // 本地IP可以访问
            byte[] data = zooKeeper.getData(path, false, null);
            System.out.println("本地IP读取数据: " + new String(data));
            
            // 查看ACL信息
            List<ACL> acls = zooKeeper.getACL(path, null);
            System.out.println("IP认证节点ACL信息:");
            printACLs(acls);
            
        } catch (Exception e) {
            System.err.println("IP认证演示失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示复合ACL（多种权限组合）
     */
    public void demonstrateCompositeACL() {
        try {
            String path = "/composite-node";
            
            // 创建复合ACL
            List<ACL> compositeACL = new ArrayList<>();
            
            // 管理员有全部权限
            String adminAuth = "admin:admin123";
            String adminDigest = DigestAuthenticationProvider.generateDigest(adminAuth);
            compositeACL.add(new ACL(ZooDefs.Perms.ALL, new Id("digest", adminDigest)));
            
            // 普通用户只有读权限
            String userAuth = "user:user123";
            String userDigest = DigestAuthenticationProvider.generateDigest(userAuth);
            compositeACL.add(new ACL(ZooDefs.Perms.READ, new Id("digest", userDigest)));
            
            // 本地IP有读写权限
            compositeACL.add(new ACL(ZooDefs.Perms.READ | ZooDefs.Perms.WRITE, 
                new Id("ip", "127.0.0.1")));
            
            // 添加管理员认证
            zooKeeper.addAuthInfo("digest", adminAuth.getBytes());
            
            zooKeeper.create(path, "复合权限数据".getBytes(), compositeACL, CreateMode.PERSISTENT);
            System.out.println("创建复合ACL节点: " + path);
            
            // 管理员可以读写
            byte[] data = zooKeeper.getData(path, false, null);
            System.out.println("管理员读取数据: " + new String(data));
            
            zooKeeper.setData(path, "管理员修改的数据".getBytes(), -1);
            System.out.println("管理员修改数据成功");
            
            // 查看ACL信息
            List<ACL> acls = zooKeeper.getACL(path, null);
            System.out.println("复合ACL节点信息:");
            printACLs(acls);
            
        } catch (Exception e) {
            System.err.println("复合ACL演示失败: " + e.getMessage());
        }
    }
    
    /**
     * 演示ACL权限检查
     */
    public void demonstratePermissionCheck() {
        try {
            // 创建一个需要特定权限的节点
            String path = "/permission-test";
            String auth = "testuser:testpass";
            String digest = DigestAuthenticationProvider.generateDigest(auth);
            
            List<ACL> acl = new ArrayList<>();
            acl.add(new ACL(ZooDefs.Perms.READ | ZooDefs.Perms.WRITE, new Id("digest", digest)));
            
            // 先添加认证信息
            zooKeeper.addAuthInfo("digest", auth.getBytes());
            
            zooKeeper.create(path, "权限测试数据".getBytes(), acl, CreateMode.PERSISTENT);
            System.out.println("创建权限测试节点: " + path);
            
            // 测试各种权限
            System.out.println("=== 权限测试 ===");
            
            // 读权限测试
            try {
                byte[] data = zooKeeper.getData(path, false, null);
                System.out.println("✓ 读权限测试通过: " + new String(data));
            } catch (KeeperException e) {
                System.out.println("✗ 读权限测试失败: " + e.getMessage());
            }
            
            // 写权限测试
            try {
                zooKeeper.setData(path, "权限测试修改".getBytes(), -1);
                System.out.println("✓ 写权限测试通过");
            } catch (KeeperException e) {
                System.out.println("✗ 写权限测试失败: " + e.getMessage());
            }
            
            // 删除权限测试（当前ACL没有删除权限）
            try {
                zooKeeper.delete(path, -1);
                System.out.println("✓ 删除权限测试通过");
            } catch (KeeperException e) {
                System.out.println("✗ 删除权限测试失败: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("权限检查演示失败: " + e.getMessage());
        }
    }
    
    /**
     * 打印ACL信息
     */
    private void printACLs(List<ACL> acls) {
        for (ACL acl : acls) {
            System.out.println("  权限: " + permissionToString(acl.getPerms()) + 
                ", 身份: " + acl.getId().getScheme() + ":" + acl.getId().getId());
        }
    }
    
    /**
     * 将权限数字转换为可读字符串
     */
    private String permissionToString(int perms) {
        StringBuilder sb = new StringBuilder();
        if ((perms & ZooDefs.Perms.READ) != 0) sb.append("READ ");
        if ((perms & ZooDefs.Perms.WRITE) != 0) sb.append("WRITE ");
        if ((perms & ZooDefs.Perms.CREATE) != 0) sb.append("CREATE ");
        if ((perms & ZooDefs.Perms.DELETE) != 0) sb.append("DELETE ");
        if ((perms & ZooDefs.Perms.ADMIN) != 0) sb.append("ADMIN ");
        return sb.toString().trim();
    }
    
    /**
     * 清理测试节点
     */
    public void cleanup() {
        String[] testPaths = {
            "/open-node", "/readonly-node", "/digest-node", 
            "/ip-node", "/composite-node", "/permission-test"
        };
        
        for (String path : testPaths) {
            try {
                if (zooKeeper.exists(path, false) != null) {
                    zooKeeper.delete(path, -1);
                    System.out.println("清理节点: " + path);
                }
            } catch (Exception e) {
                System.err.println("清理节点失败 " + path + ": " + e.getMessage());
            }
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
     * ACL演示主程序
     */
    public static void main(String[] args) {
        ZooKeeperACLDemo demo = new ZooKeeperACLDemo();
        
        try {
            demo.connect();
            
            System.out.println("=== ZooKeeper ACL 权限控制演示 ===\n");
            
            // 1. 开放ACL演示
            System.out.println("1. 开放ACL演示:");
            demo.demonstrateOpenACL();
            System.out.println();
            
            // 2. 只读ACL演示
            System.out.println("2. 只读ACL演示:");
            demo.demonstrateReadOnlyACL();
            System.out.println();
            
            // 3. Digest认证演示
            System.out.println("3. Digest认证演示:");
            demo.demonstrateDigestAuth();
            System.out.println();
            
            // 4. IP认证演示
            System.out.println("4. IP认证演示:");
            demo.demonstrateIPAuth();
            System.out.println();
            
            // 5. 复合ACL演示
            System.out.println("5. 复合ACL演示:");
            demo.demonstrateCompositeACL();
            System.out.println();
            
            // 6. 权限检查演示
            System.out.println("6. 权限检查演示:");
            demo.demonstratePermissionCheck();
            System.out.println();
            
            System.out.println("ACL演示完成，程序将在3秒后清理并退出...");
            Thread.sleep(3000);
            
            // 清理测试节点
            demo.cleanup();
            
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