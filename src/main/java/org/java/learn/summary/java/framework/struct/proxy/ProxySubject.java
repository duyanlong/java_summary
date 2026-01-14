package org.java.learn.summary.java.framework.struct.proxy;

import org.java.learn.summary.java.framework.struct.proxy.impl.RealSubject;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.struct.proxy
 * @Description: TODO
 * @date Date : 2019年06月23日 17:18
 */
public class ProxySubject implements ISubject{

    ISubject subject;

    public ProxySubject(){
        System.out.println("这是代理类");
        subject = new RealSubject();
    }

    @Override
    public void action() {
        System.out.println("代理开始");
        subject.action();
        System.out.println("代理结束");
        otherAction();
    }

    public void otherAction(){
        System.out.println("其他代理方法");
    }
}
