package com.hzc.zkpool.autoconfig;

import com.hzc.zkpool.exception.ZkConfigConvertException;
import com.hzc.zkpool.serializer.ZookeeperSerializer;
import org.springframework.core.convert.converter.Converter;

/**
 * @author: hzc
 * @Date: 2020/06/07  11:33
 * @Description:
 */
public class ZkPoolConfigConvert implements Converter<String, ZookeeperSerializer> {

    @Override
    public ZookeeperSerializer convert(String source) {
        try {
           return (ZookeeperSerializer) Class.forName(source).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new ZkConfigConvertException("class" + source + "not find");
        }
    }
}
