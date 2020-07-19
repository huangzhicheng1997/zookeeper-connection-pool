package com.hzc.zkpool.autoconfig;

import com.hzc.zkpool.serializer.KryoZookeeperSerializer;
import com.hzc.zkpool.serializer.ZookeeperSerializer;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author: hzc
 * @Date: 2020/06/03  20:13
 * @Description:
 */
@Component
@ConfigurationProperties("zookeeper-pool")
public class ZkPoolProperties {
    private Integer maxConnection=5;

    private Integer minConnection=1;

    private String znodeAddr;

    private Integer sessionTimeout=30;

    private ZookeeperSerializer zookeeperSerializerClassName=new KryoZookeeperSerializer();

    public Integer getMaxConnection() {
        return maxConnection;
    }

    public void setMaxConnection(Integer maxConnection) {
        this.maxConnection = maxConnection;
    }

    public Integer getMinConnection() {
        return minConnection;
    }

    public void setMinConnection(Integer minConnection) {
        this.minConnection = minConnection;
    }

    public String getZnodeAddr() {
        return znodeAddr;
    }

    public void setZnodeAddr(String znodeAddr) {
        this.znodeAddr = znodeAddr;
    }

    public Integer getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(Integer sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public ZookeeperSerializer getZookeeperSerializerClassName() {
        return zookeeperSerializerClassName;
    }

    public void setZookeeperSerializerClassName(ZookeeperSerializer zookeeperSerializerClassName) {
        this.zookeeperSerializerClassName = zookeeperSerializerClassName;
    }
}
