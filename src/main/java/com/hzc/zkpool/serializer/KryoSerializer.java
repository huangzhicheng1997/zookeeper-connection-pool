package com.hzc.zkpool.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.BeanSerializer;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author: hzc
 * @Date: 2020/05/30  21:21
 * @Description:
 */
public class KryoSerializer {

    private static final ThreadLocal<Kryo> localKryo=ThreadLocal.withInitial(() ->{
        Kryo kryo=new Kryo();
        kryo.setReferences(false);
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        return kryo;
    });

    public void register(Class<?> clazz){
        Kryo kryo = localKryo.get();
        kryo.register(clazz,new BeanSerializer(kryo,clazz));
    }

    public byte[] serialize(Object bean){
        Kryo kryo = localKryo.get();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream);
        kryo.writeObject(output, bean);
        output.flush();
        output.close();
        return byteArrayOutputStream.toByteArray();
    }

    public <T> T deSerialize(byte[] bytes,Class<T> clazz){
        Kryo kryo = localKryo.get();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Input input = new Input(byteArrayInputStream);
        T object = kryo.readObject(input, clazz);
        input.close();
        return object;
    }
}
