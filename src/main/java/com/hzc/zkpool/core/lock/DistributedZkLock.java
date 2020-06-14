package com.hzc.zkpool.core.lock;

import com.hzc.zkpool.core.zkpool.WatcherAdaptor;
import com.hzc.zkpool.core.zkpool.ZookeeperConnection;
import com.hzc.zkpool.core.zkpool.ZookeeperConnectionPool;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author: hzc
 * @Date: 2020/06/10  20:25
 * @Description:
 */
public class DistributedZkLock implements ZkLock {

    private String node;

    private String rootPath = "/lock";


    private ZookeeperConnection zookeeperConnection;

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public DistributedZkLock(ZookeeperConnectionPool zookeeperConnectionPool, String lockName) {
        if (!lockName.startsWith("/")) {
            throw new IllegalArgumentException("lockName illegal, name must start with '/'");
        }

        this.zookeeperConnection = zookeeperConnectionPool.getConnection();
        this.node = rootPath + lockName;
    }

    public void registerZkClientPool(ZookeeperConnectionPool zookeeperConnectionPool) throws IOException, InterruptedException {
        zookeeperConnection = zookeeperConnectionPool.getConnection();
    }

    public void getLock(String lockName) throws InterruptedException, IOException, KeeperException {
        this.node = lockName;
    }

    @Override
    public boolean tryAcquire(Long timeout, TimeUnit timeUnit) {
        try {
            zookeeperConnection.create(this.node, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            Stat stat = zookeeperConnection.exists(this.node, new WatcherAdaptor() {
                @Override
                protected void process0(WatchedEvent event, Object attachment) {

                }
            });

            if (stat != null) {
                if (this.node =)
            }


        } catch (Exception e) {
            return false;
        }


        return true;
    }

    private String getMinNode() throws KeeperException, InterruptedException {
        List<String> children = zookeeperConnection.getChildren(this.node, null);
        return Collections.min(children, (o1, o2) -> {
            if (o1.compareTo(o2) < 0) {
                return -1;
            }
            return 1;
        });
    }

    @Override
    public boolean isOccupy() {
        return false;
    }

    @Override
    public boolean timeWait() {
        return false;
    }

    @Override
    public void release() {

    }
}
