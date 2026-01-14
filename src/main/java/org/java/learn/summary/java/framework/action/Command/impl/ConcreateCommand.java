package org.java.learn.summary.java.framework.action.Command.impl;

import org.java.learn.summary.java.framework.action.Command.ICommand;
import org.java.learn.summary.java.framework.action.Command.Receiver;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.Command.impl
 * @Description: TODO
 * @date Date : 2019年06月23日 19:17
 */
public class ConcreateCommand implements ICommand {

    private Receiver receiver = null;

    public ConcreateCommand(Receiver receiver){
        this.receiver = receiver;
    }

    @Override
    public void execute() {
        receiver.action();
    }
}
