package com.hzc.zkpool.autoconfig.annotation;

import com.hzc.zkpool.autoconfig.ZkClientPoolImporter;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: hzc
 * @Date: 2020/06/08  17:15
 * @Description:
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(ZkClientPoolImporter.class)
public @interface EnableZkClientPool {
    boolean value() default true;
}
