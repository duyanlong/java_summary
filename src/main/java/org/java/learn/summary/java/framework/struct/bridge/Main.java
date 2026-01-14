package org.java.learn.summary.java.framework.struct.bridge;

import org.java.learn.summary.java.framework.struct.bridge.impl.MessageSMS;

/**
 * Created by duyanlong on 2019/6/20.
 * 桥接模式.
 */
public class Main {

    public static void main(String[] args) {
        IMessageImplementor impl1 = new MessageSMS();
        AbstractMessage message1 = new CommonMessage(impl1);
        message1.sendMessage("加班申请","李总");

        IMessageImplementor impl2 = new MessageSMS();
        AbstractMessage message2 = new UrgencyMessage(impl2);
        message2.sendMessage("加班申请","李总");
    }
}
