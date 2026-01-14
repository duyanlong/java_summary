package org.java.learn.summary.java.framework.action.Mediator.impl;

import org.java.learn.summary.java.framework.action.Mediator.Colleague;
import org.java.learn.summary.java.framework.action.Mediator.Mediator;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.Mediator.impl
 * @Description: TODO
 * @date Date : 2019年06月25日 10:50
 */
public class Colleague2 extends Colleague {

    public Colleague2(Mediator mediator) {
        super(mediator);
    }

    public void send(String message){
        mediator.send(message,this);
    }

    public void printMessage(String message){
        System.out.println("同事2得到消息：" + message);
    }
}
