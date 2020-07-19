package com.hzc.zkpool.core.lock;

import com.hzc.zkpool.core.zkpool.WatcherAdaptor;
import com.hzc.zkpool.core.zkpool.ZookeeperConnection;
import com.hzc.zkpool.logger.ZkPoolLogger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author: hzc
 * @Date: 2020/06/10  20:25
 * @Description:
 */
public class DistributedZkLock implements ZkLock {

    private ZookeeperConnection zookeeperConnection;

    private String lockName;

    private String lockPre;

    private String root = "/zkLock";

    private ThreadLocal<String> eNode = new ThreadLocal<>();


    public DistributedZkLock(ZookeeperConnection zookeeperConnection, String lockName) {
        this.zookeeperConnection = zookeeperConnection;
        if (!lockName.startsWith("/")) {
            throw new IllegalArgumentException("Path must not end with / character");
        }
        this.lockName = root + lockName;
        this.lockPre=lockName;
    }

    public void setZookeeperConnection(ZookeeperConnection zookeeperConnection) {
        this.zookeeperConnection = zookeeperConnection;
    }

    public String getLockName() {
        return lockName;
    }

    public void setLockName(String lockName) {
        if (!lockName.startsWith("/")) {
            throw new IllegalArgumentException("Path must not end with / character");
        }
        this.lockName = root + lockName;
        this.lockPre=lockName;
    }

    @Override
    public boolean lock() {
        try {
            eNode.set(zookeeperConnection.create(lockName, "", ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL));
            List<String> children = zookeeperConnection.getChildren(root, false);
            Collections.sort(children);
            String minNode = children.get(0);
            if (eNode.get().equals(root + "/" + minNode)) {
                ZkPoolLogger.info("获取锁成功");
                return true;
            }
            registerListener();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    private void registerListener() throws KeeperException, InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        String preNode = getPreNode();
        Stat stat = zookeeperConnection.exists(preNode, new WatcherAdaptor() {
            @Override
            protected void process0(WatchedEvent event, Object attachment) {
                if (Event.EventType.NodeDeleted.equals(event.getType())) {
                    countDownLatch.countDown();
                }
            }
        },this);
       if (stat!=null) {
           countDownLatch.await();
       }
    }


    private static void getNode(String node, int index, StringBuilder sb) {
        if (node.charAt(index) == '/') {
            return;
        }
        getNode(node, index - 1, sb);
        char c = node.charAt(index);
        if (c != '/') {
            sb.append(c);
        }
    }

    private String getPreNode() {
        StringBuilder sb = new StringBuilder();
        getNode(eNode.get(), eNode.get().length() - 1, sb);
        String currNode = sb.toString();
        String[] split = lockName.split("/");
        String seq = currNode.substring(split[split.length-1].length());

        StringBuilder nub = new StringBuilder(String.valueOf(Long.parseLong(seq) - 1));
        int length = nub.length();
        for (int i = 0; i < 10 - length; i++) {
            nub.insert(0, "0");
        }

        return root+lockPre+nub;
    }


    @Override
    public void release() {
        try {
            zookeeperConnection.deleteNode(eNode.get(), -1);
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }


//    private String node;
//
//    private ThreadLocal<String> eNod = new ThreadLocal<>();
//
//    private String rootPath = "/lock";
//
//    private ThreadLocal<CountDownLatch> countDownLatchThreadLocal = new ThreadLocal<>();
//
//    private ZookeeperConnection zookeeperConnection;
//
//
//    public DistributedZkLock(ZookeeperConnectionPool zookeeperConnectionPool, String lockName) {
//        if (!lockName.startsWith("/")) {
//            throw new IllegalArgumentException("lockName illegal, name must start with '/'");
//        }
//
//        try {
//            this.zookeeperConnection = zookeeperConnectionPool.getConnection();
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }
//        this.node = rootPath + lockName;
//    }
//
//    public void registerZkClientPool(ZookeeperConnectionPool zookeeperConnectionPool) throws IOException, InterruptedException {
//        zookeeperConnection = zookeeperConnectionPool.getConnection();
//    }
//
//    public void getLock(String lockName) throws InterruptedException, IOException, KeeperException {
//        this.node = lockName;
//    }
//
//    @Override
//    public boolean tryAcquire(Long timeout, TimeUnit timeUnit) {
//
//        try {
//            Semaphore semaphore = new Semaphore(1);
//            semaphore.acquire(1);
//            eNod.set(zookeeperConnection.create(node, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL));
//            return check(semaphore,eNod.get());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    private boolean check(Semaphore semaphore,String mNode) throws KeeperException, InterruptedException {
//       CountDownLatch countDownLatch=new CountDownLatch(1);
//        Stat stat = zookeeperConnection.exists(mNode, new WatcherAdaptor() {
//            @Override
//            protected void process0(WatchedEvent event, Object attachment) {
//                if (Event.EventType.NodeDeleted.equals(event.getType())) {
//                    countDownLatch.countDown();
//                    System.out.println("xxxx");
//                }
//            }
//        }, this);
//
//        String minNode = getMinNode();
//        String endNode = getEndNode();
//        if (endNode.equals(minNode)) {
//            ZkPoolLogger.info("获取锁成功");
//            return true;
//        }
//
//        countDownLatch.await();
//        System.out.println("xxxxxa");
//        return check(semaphore,minNode);
//    }
//
//    private String getMinNode() throws KeeperException, InterruptedException {
//        String lastNode = getLastNode();
//        List<String> children = zookeeperConnection.getChildren(lastNode, null);
//        return Collections.min(children, (o1, o2) -> {
//            if (o1.compareTo(o2) < 0) {
//                return -1;
//            }
//            return 1;
//        });
//    }
//
//    private String getLastNode() {
//        String[] nodes = node.split("/");
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < nodes.length - 1; i++) {
//            if (!StringUtils.isEmpty(nodes[i])) {
//                sb.append("/").append(nodes[i]);
//            }
//        }
//        return sb.toString();
//    }
//
//    private String getEndNode() {
//        String[] nodes = eNod.get().split("/");
//        return nodes[nodes.length - 1];
//    }
//
//    @Override
//    public void release() {
//        try {
//            zookeeperConnection.deleteNode(eNod.get(), -1);
//            zookeeperConnection.release();
//            ZkPoolLogger.info("释放锁成功");
//        } catch (KeeperException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }


}
