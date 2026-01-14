package org.java.learn.summary.java.framework.struct.bridge;

/**
 * Created by duyanlong on 2019/6/20.
 */
public class UrgencyMessage extends AbstractMessage {

    public UrgencyMessage(IMessageImplementor impl) {
        super(impl);
    }

    @Override
    void sendMessage(String message, String toUser) {
        message = "加急" + message;
        super.sendMessage(message, toUser);
    }

    public Object watch(){
        return "处理成功";
    }
}
