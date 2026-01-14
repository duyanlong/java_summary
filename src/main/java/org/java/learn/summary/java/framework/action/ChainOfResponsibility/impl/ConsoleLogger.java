package org.java.learn.summary.java.framework.action.ChainOfResponsibility.impl;

import org.java.learn.summary.java.framework.action.ChainOfResponsibility.AbstractLogger;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.ChainOfResponsibility.impl
 * @Description: TODO
 * @date Date : 2019年06月23日 18:47
 */
public class ConsoleLogger extends AbstractLogger {

    public ConsoleLogger(int level){
        this.level = level;
    }

    @Override
    protected void write(String message) {
        System.out.println("Console message = [" + message + "]");
    }
}
