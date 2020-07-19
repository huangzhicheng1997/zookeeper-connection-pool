package com.hzc.zkpool.core.lock;

import org.apache.zookeeper.KeeperException;

import java.io.IOException;

/**
 * @author: hzc
 * @Date: 2020/06/10  19:24
 * @Description:
 */
public interface ZkLock {


    boolean lock() throws InterruptedException, IOException, KeeperException;

    void release();

}
