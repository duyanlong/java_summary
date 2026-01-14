package org.java.learn.summary.java.framework.struct.bridge;

/**
 * Created by duyanlong on 2019/6/20.
 */
public abstract class AbstractMessage {

    IMessageImplementor impl;
    public AbstractMessage(IMessageImplementor impl){
        this.impl = impl;
    }

    void sendMessage(String message,String toUser){
        impl.send(message,toUser);
    }
}
