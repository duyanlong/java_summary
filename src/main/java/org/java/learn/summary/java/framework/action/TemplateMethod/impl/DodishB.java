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
public class DodishB extends DodishTemplate {

    @Override
    public void prepare() {
        System.out.println("洗尖椒和土豆丝，切土豆！");
    }

    @Override
    public void doing() {
        System.out.println("开始炒菜，先放油再青椒、土豆丝");
    }

    @Override
    public void clear() {
        System.out.println("清理");
    }
}
