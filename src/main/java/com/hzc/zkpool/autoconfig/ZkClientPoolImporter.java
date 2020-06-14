package com.hzc.zkpool.autoconfig;

import com.hzc.zkpool.autoconfig.annotation.ZkClientSwitch;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author: hzc
 * @Date: 2020/06/08  17:17
 * @Description:
 */
@Component
public class ZkClientPoolImporter implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes("com.hzc.zkpool.autoconfig.annotation.EnableZkClientPool");
        Boolean flag = (Boolean) annotationAttributes.get("value");
        if (flag){
            AbstractBeanDefinition singleton = BeanDefinitionBuilder.genericBeanDefinition(ZkClientSwitch.class)
                    .setScope("singleton").getBeanDefinition();
            registry.registerBeanDefinition("zkClientSwitch",singleton);
        }
    }
}
