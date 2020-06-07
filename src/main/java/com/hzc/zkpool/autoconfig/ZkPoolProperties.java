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
@Data
@Component
@ConfigurationProperties("zookeeper-pool")
public class ZkPoolProperties {
    private Integer maxConnection=5;

    private Integer minConnection=1;

    private String znodeAddr;

    private Integer sessionTimeout=30;

    private ZookeeperSerializer zookeeperSerializerClassName=new KryoZookeeperSerializer();
}
