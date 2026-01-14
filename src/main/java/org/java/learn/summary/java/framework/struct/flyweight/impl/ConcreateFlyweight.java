package org.java.learn.summary.java.framework.struct.flyweight.impl;

import org.java.learn.summary.java.framework.struct.flyweight.IFlyweight;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.struct.flyweight.impl
 * @Description: TODO
 * @date Date : 2019年06月23日 16:30
 */
public class ConcreateFlyweight implements IFlyweight {

    private String key;
    public ConcreateFlyweight(String key){
        this.key = key;
    }

    @Override
    public void operation(String message) {
        System.out.println("类key为： = [" + key + "]");
        System.out.println("输入内容为" + message);
    }
}
