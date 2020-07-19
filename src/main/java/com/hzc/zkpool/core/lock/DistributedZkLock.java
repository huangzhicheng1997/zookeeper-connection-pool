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


    private  void getNode(String node, int index, StringBuilder sb) {
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
}
