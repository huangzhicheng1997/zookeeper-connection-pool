package com.hzc.zkpool.core.zkpool;

import com.hzc.zkpool.logger.ZkPoolLogger;
import com.hzc.zkpool.serializer.MessagePackZookeeperSerializer;
import com.hzc.zkpool.serializer.ZookeeperSerializer;
import com.hzc.zkpool.exception.ZookeeperConnectionException;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author: hzc
 * @Date: 2020/05/29  10:50
 * @Description: zookeeper客户端连接池，充分利用带宽
 */
public class ZookeeperConnectionPool {

    private List<ZookeeperConnection> zookeeperConnectionList = new CopyOnWriteArrayList<>();

    /**
     * 最大连接数
     */
    private Integer maxConnection = 5;

    /**
     * 最小连接数
     */
    private Integer minConnection = 3;

    /**
     * 监视器
     */
    private final Object monitor = new Object();

    /**
     * Zookeeper节点地址
     */
    private String znodeAddr;

    /**
     * 心跳时间
     */
    private Integer sessionTimeout;

    /**
     * 上一次获取的连接
     */
    private ZookeeperConnection lastConnection;

    /**
     * 默认序列化为MessagePack
     */
    private static final ZookeeperSerializer DEFAULT_SERIALIZER = new MessagePackZookeeperSerializer();

    private ZookeeperSerializer zookeeperSerializer = DEFAULT_SERIALIZER;

    public ZookeeperConnectionPool(Integer maxConnection, Integer minConnection, String znodeAddr, Integer sessionTimeout) throws IOException, InterruptedException {
        this.maxConnection = maxConnection;
        this.minConnection = minConnection;
        this.znodeAddr = znodeAddr;
        this.sessionTimeout = sessionTimeout;
        init();

    }

    public ZookeeperConnectionPool(Integer maxConnection, Integer minConnection, String znodeAddr, Integer sessionTimeout, ZookeeperSerializer zookeeperSerializer) throws IOException, InterruptedException {
        this.maxConnection = maxConnection;
        this.minConnection = minConnection;
        this.znodeAddr = znodeAddr;
        this.sessionTimeout = sessionTimeout;
        this.zookeeperSerializer = zookeeperSerializer;
        init();

    }

    private void init() throws IOException, InterruptedException {
        for (int i = 0; i < minConnection; i++) {
            ZookeeperConnection zookeeperConnection = new ZookeeperConnection(sessionTimeout, znodeAddr);
            zookeeperConnection.registerSerializer(zookeeperSerializer);
            zookeeperConnection.setZookeeperConnectionPool(this);
            zookeeperConnectionList.add(zookeeperConnection);
        }
    }


    public ZookeeperConnection getConnection() throws IOException, InterruptedException {
        ZookeeperConnection zookeeperConnection = doGetConnection();
        zookeeperConnection.acquire();
        ZkPoolLogger.debug("connect: {} is using",zookeeperConnection.getConnectionSeq());
        return zookeeperConnection;
    }



    /**
     * 获取zookeeperClient实例
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private ZookeeperConnection doGetConnection() throws IOException, InterruptedException {
        //寻找空闲连接
        ZookeeperConnection connection = findFreeConnection();
        if (null != connection) {
            lastConnection=connection;
            return connection;
        }

        if (zookeeperConnectionList.size() == maxConnection) {
            //连接池已满，而且未有空闲连接，寻找最小负载的连接
            ZookeeperConnection zookeeperConnection = loadBalance();
            if (null == zookeeperConnection) {
                throw new ZookeeperConnectionException("load balance error");
            }
            //不是0 说明前面有失效连接
            int firstAliveIndex = zookeeperConnectionList.indexOf(zookeeperConnection);
            if (0 != firstAliveIndex) {
                zookeeperConnectionList = zookeeperConnectionList.subList(firstAliveIndex, zookeeperConnectionList.size());
            }
            lastConnection=zookeeperConnection;
            return zookeeperConnection;
        }

        synchronized (monitor) {
            //未找到空闲连接，创建一条空闲连接
            if (zookeeperConnectionList.size() < maxConnection) {
                ZookeeperConnection zookeeperConnection = new ZookeeperConnection(sessionTimeout, znodeAddr);
                zookeeperConnection.registerSerializer(zookeeperSerializer);
                zookeeperConnection.setZookeeperConnectionPool(this);
                zookeeperConnectionList.add(zookeeperConnection);
                lastConnection=zookeeperConnection;
                return zookeeperConnection;
            }
        }

        return doGetConnection();
    }

    /**
     * 获取上一次使用的connection
     *
     * @return
     */
    public ZookeeperConnection getLastConnection() {
        return lastConnection;
    }


    /**
     * 保证 所有的连接都能被使用
     *
     * @return
     */
    private ZookeeperConnection findFreeConnection() {
        if (CollectionUtils.isEmpty(zookeeperConnectionList)) {
            return null;
        }

        for (ZookeeperConnection zookeeperConnection : zookeeperConnectionList) {
            if (!zookeeperConnection.inUsing() && zookeeperConnection.getConnectionState().isAlive()) {
                return zookeeperConnection;
            }
        }
        return null;
    }


    /**
     * 获取负载最小连接
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private ZookeeperConnection loadBalance() throws IOException, InterruptedException {
        return doLoadBalance(0);
    }

    /**
     * 获取负载最小连接
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    private ZookeeperConnection doLoadBalance(int times) throws IOException, InterruptedException {
        if (CollectionUtils.isEmpty(zookeeperConnectionList)) {
            return null;
        }

        //排序
        zookeeperConnectionList.sort(Comparator.comparing(ZookeeperConnection::getWorkLoad));

        ZookeeperConnection targetConnection = null;
        //获取负载最小的连接
        for (ZookeeperConnection zookeeperConnection : zookeeperConnectionList) {
            if (zookeeperConnection.getConnectionState().isAlive()) {
                targetConnection = lastConnection = zookeeperConnection;
                break;
            } else {
                zookeeperConnection.closeInAnyCase();
            }
        }

        if (targetConnection != null) {
            return targetConnection;
        }

        //控制递归次数最多递归1次
        if (lastConnection == null && times != 0) {
            return null;
        }

        //连接全部失效
        synchronized (monitor) {
            //检查是否已经可以获取连接了
            ZookeeperConnection zookeeperConnection = doLoadBalance(++times);
            if (zookeeperConnection != null && zookeeperConnection.getConnectionState().isAlive()) {
                return zookeeperConnection;
            }

            //清空连接列表
            zookeeperConnectionList.clear();

            return doGetConnection();
        }
    }

}
