package org.java.learn.summary.java.framework.action.Mediator;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.Mediator
 * @Description: TODO
 * @date Date : 2019年06月25日 10:46
 */
public abstract class Mediator {

    public abstract void setColleague1(Colleague colleague1);
    public abstract void setColleague2(Colleague colleague2);

    public abstract void send(String message,Colleague colleague);
}
