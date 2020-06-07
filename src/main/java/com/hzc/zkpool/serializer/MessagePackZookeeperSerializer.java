package com.hzc.zkpool.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import org.msgpack.MessagePack;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author: hzc
 * @Date: 2020/05/30  20:26
 * @Description:
 */
public class MessagePackZookeeperSerializer implements ZookeeperSerializer {
    private MessagePack messagePack = new MessagePack();


    @Override
    public byte[] serializer(Object data) throws IOException {
        return messagePack.write(data);
    }

    @Override
    public <T> T deSerializer(byte[] data, Class<T> clazz) throws IOException {
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeBytes(data);
        ByteBuffer byteBuffer = buffer.nioBuffer();
        T read = messagePack.read(byteBuffer,clazz);
        buffer.release();
        return read;
    }
}
