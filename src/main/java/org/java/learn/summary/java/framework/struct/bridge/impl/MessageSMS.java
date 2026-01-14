package org.java.learn.summary.java.framework.struct.bridge.impl;

import org.java.learn.summary.java.framework.struct.bridge.IMessageImplementor;

/**
 * Created by duyanlong on 2019/6/20.
 */
public class MessageSMS implements IMessageImplementor {

    @Override
    public void send(String message, String toUser) {
        System.out.println("使用系统内短消息的方法，发送消息'" + message + "' 给" + toUser);
    }
}
