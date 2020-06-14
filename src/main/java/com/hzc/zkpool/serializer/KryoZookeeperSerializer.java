package com.hzc.zkpool.serializer;

import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.Data;

import java.io.IOException;

/**
 * @author: hzc
 * @Date: 2020/05/30  20:58
 * @Description:
 */
public class KryoZookeeperSerializer implements ZookeeperSerializer {
    private KryoSerializer kryoSerializer = new KryoSerializer();

    @Override
    public byte[] serializer(Object data)  {
        if (!(data instanceof Byte) && !(data instanceof Short) && !(data instanceof Integer) && !(data instanceof Long) && !(data instanceof Float) && !(data instanceof Double) && !(data instanceof Boolean) && !(data instanceof CharSequence)){
            kryoSerializer.register(data.getClass());
        }

        return kryoSerializer.serialize(data);
    }

    @Override
    public <T> T deSerializer(byte[] data, Class<T> clazz) throws IOException {
        return kryoSerializer.deSerialize(data, clazz);
    }
}