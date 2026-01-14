package org.java.learn.summary.java.framework.action.ChainOfResponsibility.impl;

import org.java.learn.summary.java.framework.action.ChainOfResponsibility.AbstractLogger;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.ChainOfResponsibility.impl
 * @Description: TODO
 * @date Date : 2019年06月23日 18:49
 */
public class ErrorLogger extends AbstractLogger {

    public ErrorLogger(int level){
        this.level = level;
    }

    @Override
    protected void write(String message) {

        System.out.println("error message = [" + message + "]");
    }
}
