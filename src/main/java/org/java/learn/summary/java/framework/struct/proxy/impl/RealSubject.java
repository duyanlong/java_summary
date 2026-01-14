package org.java.learn.summary.java.framework.struct.proxy.impl;

import org.java.learn.summary.java.framework.struct.proxy.ISubject;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.struct.proxy.impl
 * @Description: TODO
 * @date Date : 2019年06月23日 17:13
 */
public class RealSubject implements ISubject {

    @Override
    public void action() {
        System.out.println("这是实体类");
    }
}
