package com.hzc.zkpool.autoconfig;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: hzc
 * @Date: 2020/06/07  11:54
 * @Description:
 */
@Configuration
public class ZkPoolPropertiesConfig {
    @Bean
    @ConfigurationPropertiesBinding
    public ZkPoolConfigConvert zkPoolConfigConvert(){
        return new ZkPoolConfigConvert();
    }
}
