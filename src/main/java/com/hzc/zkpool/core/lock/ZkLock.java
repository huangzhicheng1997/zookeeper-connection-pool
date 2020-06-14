package com.hzc.zkpool.core.lock;

import com.hzc.zkpool.core.zkpool.ZookeeperConnectionPool;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author: hzc
 * @Date: 2020/06/10  19:24
 * @Description:
 */
public interface ZkLock {


    boolean tryAcquire(Long timeout, TimeUnit timeUnit) throws InterruptedException, IOException, KeeperException;

    void release();

}
