package org.java.learn.summary.java.framework.action.Mediator;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.Mediator
 * @Description: TODO
 * @date Date : 2019年06月25日 10:47
 */
public abstract class Colleague {

    protected  Mediator mediator;
    public Colleague(Mediator mediator){
        this.mediator = mediator;
    }

    public abstract void send(String message);

    public abstract void printMessage(String message);

}
