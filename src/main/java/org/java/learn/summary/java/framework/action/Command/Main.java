package org.java.learn.summary.java.framework.action.Command;

import org.java.learn.summary.java.framework.action.Command.impl.ConcreateCommand;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.ChainOfResponsibility
 * @Description: TODO
 * @date Date : 2019年06月23日 17:36
 * 命令模式.
 */
public class Main {

    public static void main(String[] args) {
        Receiver receiver = new Receiver();
        ICommand command = new ConcreateCommand(receiver);
        Invoker invoker = new Invoker(command);
        invoker.action();

    }
}
