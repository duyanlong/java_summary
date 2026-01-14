package org.java.learn.summary.java.framework.action.TemplateMethod.impl;

import org.java.learn.summary.java.framework.action.TemplateMethod.DodishTemplate;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.TemplateMethod.impl
 * @Description: TODO
 * @date Date : 2019年07月01日 20:17
 */
public class DodishA extends DodishTemplate {

    @Override
    public void prepare() {
        System.out.println("洗西红柿，准备鸡蛋！");
    }

    @Override
    public void doing() {
        System.out.println("开始炒菜，先放油再放蛋");
    }

    @Override
    public void clear() {
        System.out.println("清理");
    }
}
