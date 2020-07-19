package com.hzc.zkpool.logger;

import com.hzc.zkpool.core.zkpool.WatcherAdaptor;
import com.hzc.zkpool.core.zkpool.ZookeeperConnection;
import com.hzc.zkpool.core.zkpool.ZookeeperConnectionPool;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author: hzc
 * @Date: 2020/06/03  19:22
 * @Description:
 */
public class ZkPoolLogger {
    private static final Logger logger = LoggerFactory.getLogger(ZkPoolLogger.class);

    public static Logger getLogger() {
        return logger;
    }

    public static void info(String msg, Object o) {
        String replace = msg.replace("{}", o.toString());
        logger.info(replace);
    }

    public static void info(String msg) {
        logger.info(msg);
    }

    public static void debug(String msg,Object o){
        String replace = msg.replace("{}", o.toString());
        logger.debug(replace);
    }


    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        ZookeeperConnectionPool zookeeperConnectionPool=new ZookeeperConnectionPool(1,1,"localhost:2181",3000);
        ZookeeperConnection connection = zookeeperConnectionPool.getConnection();
        String sada = connection.create("/data/xx/a", "sada", ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

      TimeUnit.SECONDS.sleep(10000);
    }

}
