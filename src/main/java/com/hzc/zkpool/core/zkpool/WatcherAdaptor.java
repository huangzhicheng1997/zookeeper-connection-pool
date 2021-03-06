package com.hzc.zkpool.core.zkpool;

import com.sun.istack.internal.Nullable;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;


/**
 * @author: hzc
 * @Date: 2020/05/28  20:35
 * @Description:
 */
public abstract class WatcherAdaptor implements Watcher {
    /**
     * 透传
     */
    private Object attachment;

    public WatcherAdaptor() {
    }

    public void setAttachment(Object attachment) {
        this.attachment = attachment;
    }

    @Override
    public void process(WatchedEvent event) {
        process0(event, attachment);
    }

    protected abstract void process0(WatchedEvent event, @Nullable Object attachment);
}
