package org.java.learn.summary.java.framework.action.Strategy.impl;

import org.java.learn.summary.java.framework.action.Strategy.IStrategy;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.Strategy.impl
 * @Description: TODO
 * @date Date : 2019年07月01日 20:03
 */
public class ConcreateStrategyB implements IStrategy {

    @Override
    public void algorithm() {
        System.out.println("StrategyB");
    }
}
