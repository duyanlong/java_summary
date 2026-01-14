package org.java.learn.summary.java.framework.struct.bridge;

/**
 * Created by duyanlong on 2019/6/20.
 */
public class CommonMessage extends AbstractMessage {

    public CommonMessage(IMessageImplementor impl) {
        super(impl);
    }

    @Override
    void sendMessage(String message, String toUser) {
        super.sendMessage(message, toUser);
    }
}
