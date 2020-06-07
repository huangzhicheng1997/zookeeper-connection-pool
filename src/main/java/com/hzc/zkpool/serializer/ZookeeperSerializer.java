package com.hzc.zkpool.serializer;

import java.io.IOException;

/**
 * @author: hzc
 * @Date: 2020/05/30  20:19
 * @Description:
 */
public interface ZookeeperSerializer {

    public byte[] serializer(Object data) throws IOException;

    public <T> T deSerializer(byte[] data, Class<T> clazz) throws IOException;
}
