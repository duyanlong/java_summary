package org.java.learn.summary.java.framework.struct.proxy;

/**
 * Created by duyanlong on 2019/6/20.
 * 代理模式.
 */
public class Main {

    public static void main(String[] args) {

        ISubject subject = new ProxySubject();
        subject.action();
    }
}
