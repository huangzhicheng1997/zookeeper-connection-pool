package com.hzc.zkpool.autoconfig;


import com.hzc.zkpool.core.zkpool.ZookeeperConnection;
import com.hzc.zkpool.core.zkpool.ZookeeperConnectionPool;
import com.hzc.zkpool.exception.ZkPoolInitException;
import com.hzc.zkpool.serializer.ZookeeperSerializer;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * @author: hzc
 * @Date: 2020/06/03  19:32
 * @Description:
 */
@Configuration
@EnableConfigurationProperties(ZkPoolProperties.class)
@ConditionalOnMissingBean({ZookeeperConnectionPool.class, ZooKeeper.class, ZookeeperConnection.class})
public class ZookeeperConnectionPoolConfig {

    @Autowired
    private ZkPoolProperties zkPoolProperties;

    @Bean
    public ZookeeperConnectionPool zookeeperConnectionPoolFactory() throws IOException, InterruptedException {
        ZookeeperSerializer zookeeperSerializer = zkPoolProperties.getZookeeperSerializerClassName();
        String znodeAddr = zkPoolProperties.getZnodeAddr();
        if (StringUtils.isEmpty(znodeAddr)) {
            throw new ZkPoolInitException("no zNode addr defined");
        }

        return new ZookeeperConnectionPool(zkPoolProperties.getMaxConnection(),
                zkPoolProperties.getMinConnection(), zkPoolProperties.getZnodeAddr(),
                zkPoolProperties.getSessionTimeout()*1000*60, zookeeperSerializer);
    }
}
