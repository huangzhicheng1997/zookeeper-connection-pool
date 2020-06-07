package com.hzc.zkpool.core.zkpool;


import com.hzc.zkpool.serializer.ZookeeperSerializer;
import com.hzc.zkpool.exception.ZookeeperConnectionException;
import com.hzc.zkpool.exception.ZookeeperConnectionReleaseException;
import com.hzc.zkpool.logger.ZkPoolLogger;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: hzc
 * @Date: 2020/05/28  20:18
 * @Description:
 */
public class ZookeeperConnection {

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private static final AtomicInteger counter = new AtomicInteger(0);

    private ZooKeeper zooKeeper;

    private Integer sessionTimeout;

    private String znodeAddr;

    private Integer connectionSeq;


    private ZookeeperSerializer zookeeperSerializer;

    /**
     * 表示是哪个连接池的对象
     */
    private ZookeeperConnectionPool zookeeperConnectionPool;

    /**
     * 引用计数
     */
    private volatile AtomicInteger IN_USE = new AtomicInteger(0);


    public ZookeeperConnection(Integer sessionTimeout, String znodeAddr) throws IOException, InterruptedException {
        this.sessionTimeout = sessionTimeout;
        this.znodeAddr = znodeAddr;
        init();
    }

    /**
     * 初始化，与zookeeper建立连接，建立连接过程中阻塞
     *
     * @throws IOException
     * @throws InterruptedException
     */
    private void init() throws IOException, InterruptedException {
        connectionSeq = counter.getAndIncrement();
        Watcher connectingWatcher = event -> {
            if (event.getState().equals(Watcher.Event.KeeperState.SyncConnected)) {
                countDownLatch.countDown();
                ZkPoolLogger.info("zkClient-{} is connected",connectionSeq);
            }
        };
        this.zooKeeper = new ZooKeeper(znodeAddr, sessionTimeout, connectingWatcher);
        //连接超时释放资源
        if (!countDownLatch.await(sessionTimeout, TimeUnit.MILLISECONDS)) {
            zooKeeper.close();
            throw new ZookeeperConnectionException("connect to zkServer timeout");
        }
    }

    /**
     * 创建节点
     *
     * @param path       路径
     * @param data       节点的数据
     * @param acl        acl权限控制
     * @param createMode 节点类型{@link CreateMode}
     * @return 节点路劲
     * @throws KeeperException
     * @throws InterruptedException
     * @throws IOException
     */
    public String create(String path, Object data, List<ACL> acl, CreateMode createMode) throws KeeperException, InterruptedException, IOException {
        return zooKeeper.create(path, zookeeperSerializer.serializer(data), acl, createMode);
    }

    /**
     * 创建节点,异步回调
     *
     * @param path           路劲
     * @param data           节点数据
     * @param acl            acl权限控制
     * @param createMode     节点类型{@link CreateMode}
     * @param stringCallback 异步回调函数
     * @param ctx            透传对象
     * @return
     * @throws IOException
     */
    public void create(String path, Object data, List<ACL> acl, CreateMode createMode, AsyncCallback.StringCallback stringCallback, Object ctx) throws IOException {
        zooKeeper.create(path, zookeeperSerializer.serializer(data), acl, createMode, stringCallback, ctx);
    }

    /**
     * 删除节点 (节点有数据不能删)
     *
     * @param path    节点路径
     * @param version 版本号，用于乐观锁
     * @throws KeeperException
     * @throws InterruptedException
     */
    public void deleteNode(String path, int version) throws KeeperException, InterruptedException {
        zooKeeper.delete(path, version);
    }


    public void deleteNode(String path, int version, AsyncCallback.VoidCallback voidCallback, Object ctx) throws KeeperException, InterruptedException {
        zooKeeper.delete(path, version, voidCallback, ctx);
    }


    /**
     * 检查节点是否存在
     *
     * @param path
     * @param watcher
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public Stat exists(String path, Watcher watcher) throws KeeperException, InterruptedException {
        return zooKeeper.exists(path, watcher);
    }

    public Stat exist(String path) throws KeeperException, InterruptedException {
        return zooKeeper.exists(path, false);
    }

    public void exist(String path, Watcher watcher, AsyncCallback.StatCallback statCallback, Object ctx) {
        zooKeeper.exists(path, watcher, statCallback, ctx);

    }

    public void exist(String path, AsyncCallback.StatCallback statCallback, Object ctx) {
        zooKeeper.exists(path, false, statCallback, ctx);
    }


    public Stat setData(String path, Object data, int version) throws IOException, KeeperException, InterruptedException {

        return zooKeeper.setData(path, zookeeperSerializer.serializer(data), version);
    }

    public void setData(String path, Object data, int version, AsyncCallback.StatCallback statCallback, Object ctx) throws IOException, KeeperException, InterruptedException {
        zooKeeper.setData(path, zookeeperSerializer.serializer(data), version, statCallback, ctx);
    }


    /**
     * 获取数据,并设置一个监视器，数据发生改变时就会触发
     *
     * @param path
     * @param watcher
     * @param stat
     * @param clazz
     * @param <T>
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     * @throws IOException
     */
    public <T> T getData(String path, Watcher watcher, Stat stat, Class<T> clazz) throws KeeperException, InterruptedException, IOException {
        byte[] data = zooKeeper.getData(path, watcher, stat);
        return zookeeperSerializer.deSerializer(data, clazz);
    }

    public List<String> getChildren(String path, Watcher watcher) throws KeeperException, InterruptedException {
        return zooKeeper.getChildren(path, watcher);
    }


    /**
     * 异步获取数据
     *
     * @param path
     * @param watcher
     * @param dataCallback
     * @param ctx
     * @throws KeeperException
     * @throws InterruptedException
     * @throws IOException
     */
    public void getDataAsync(String path, Watcher watcher, AsyncCallback.DataCallback dataCallback, Object ctx) throws KeeperException, InterruptedException, IOException {
        zooKeeper.getData(path, watcher, dataCallback, ctx);
    }


    /**
     * 获取数据简单方法
     *
     * @param path
     * @param clazz
     * @param <T>
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     * @throws IOException
     */
    public <T> T getDataSimple(String path, Class<T> clazz) throws KeeperException, InterruptedException, IOException {
        byte[] data = zooKeeper.getData(path, false, null);
        return zookeeperSerializer.deSerializer(data, clazz);
    }

    /**
     * 获取数据
     *
     * @param path
     * @param clazz
     * @param <T>
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     * @throws IOException
     */
    public <T> T getData(String path, Stat stat, Class<T> clazz) throws KeeperException, InterruptedException, IOException {
        byte[] data = zooKeeper.getData(path, false, stat);
        return zookeeperSerializer.deSerializer(data, clazz);
    }


    /**
     * 安全关闭（检测是否处于空闲状态）
     *
     * @throws InterruptedException
     */
    public void closeSafely() throws InterruptedException, ZookeeperConnectionReleaseException {
        if (IN_USE.get() != 0) {
            throw new ZookeeperConnectionException("this connection is using");
        }
        if (zookeeperConnectionPool == null) {
            throw new ZookeeperConnectionException("no pool connection");
        }
        this.zooKeeper.close();
    }

    /**
     * 强制关闭
     *
     * @throws InterruptedException
     */
    public void closeInAnyCase() throws InterruptedException {
        if (zookeeperConnectionPool == null) {
            throw new ZookeeperConnectionException("no pool connection");
        }
        this.zooKeeper.close();
    }

    /**
     * 释放资源 ,程序执行完毕执行一下release有助于负载均衡
     */
    public void release() {
        if (null == zooKeeper) {
            throw new ZookeeperConnectionException("client not defined");
        }
        if (IN_USE.decrementAndGet() < 0) {
            throw new ZookeeperConnectionException("release overflow IN_USE < 0");
        }
    }

    /**
     * 置为使用中
     */
    void acquire() {
        if (null == zooKeeper) {
            throw new ZookeeperConnectionException("client not defined");
        }
        IN_USE.incrementAndGet();
    }

    /**
     * 检查是否处于空闲
     *
     * @return
     */
    public boolean inUsing() {
        return !(IN_USE.get() == 0);
    }

    /**
     * 获取引用数即工作负载，用于负载均衡
     *
     * @return
     */
    public Integer getWorkLoad() {
        return IN_USE.get();
    }

    /**
     * 获取序列化器
     *
     * @return
     */
    public ZookeeperSerializer getZookeeperSerializer() {
        return zookeeperSerializer;
    }

    /**
     * 设置序列化器
     *
     * @param zookeeperSerializer
     */
    public void registerSerializer(ZookeeperSerializer zookeeperSerializer) {
        this.zookeeperSerializer = zookeeperSerializer;
    }

    /**
     * 获取连接状态
     *
     * @return
     */
    public ZooKeeper.States getConnectionState() {
        return this.zooKeeper.getState();
    }

    public ZookeeperConnectionPool getZookeeperConnectionPool() {
        return zookeeperConnectionPool;
    }

    public void setZookeeperConnectionPool(ZookeeperConnectionPool zookeeperConnectionPool) {
        this.zookeeperConnectionPool = zookeeperConnectionPool;
    }

    public Integer getConnectionSeq() {
        return connectionSeq;
    }
}
