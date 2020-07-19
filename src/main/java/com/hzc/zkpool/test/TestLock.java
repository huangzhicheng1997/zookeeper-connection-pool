package com.hzc.zkpool.test;

import com.hzc.zkpool.core.lock.DistributedZkLock;
import com.hzc.zkpool.core.zkpool.ZookeeperConnection;
import com.hzc.zkpool.core.zkpool.ZookeeperConnectionPool;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestLock {
    public static  int a=0;

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        ZookeeperConnectionPool zookeeperConnectionPool = new ZookeeperConnectionPool(3, 3, "127.0.0.1:2181", 10000);
        ZookeeperConnection connection = zookeeperConnectionPool.getConnection();
        DistributedZkLock lock = new DistributedZkLock(connection, "/lock");
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        for (int i=0;i<1000;i++) {
            executorService.execute(() -> {
                lock.lock();
                System.out.println(++TestLock.a);
                lock.release();
            });
        }
        connection.release();

    }
}
