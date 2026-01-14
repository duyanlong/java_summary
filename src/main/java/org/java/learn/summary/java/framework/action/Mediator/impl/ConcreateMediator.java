package org.java.learn.summary.java.framework.action.Mediator.impl;

import org.java.learn.summary.java.framework.action.Mediator.Colleague;
import org.java.learn.summary.java.framework.action.Mediator.Mediator;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.Mediator.impl
 * @Description: TODO
 * @date Date : 2019年06月25日 10:52
 */
public class ConcreateMediator extends Mediator {

    private Colleague colleague1;
    private Colleague colleague2;

    public void setColleague1(Colleague colleague1){
        this.colleague1 = colleague1;
    }

    public void setColleague2(Colleague colleague2){
        this.colleague2 = colleague2;
    }

    @Override
    public void send(String message, Colleague colleague) {

        if(colleague == colleague1){
            colleague2.printMessage(message);
        }else{
            colleague1.printMessage(message);
        }
    }
}
