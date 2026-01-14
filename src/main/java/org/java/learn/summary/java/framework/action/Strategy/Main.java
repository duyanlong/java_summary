package org.java.learn.summary.java.framework.action.Strategy;

import org.java.learn.summary.java.framework.action.Strategy.impl.ConcreateStrategyA;
import org.java.learn.summary.java.framework.action.Strategy.impl.ConcreateStrategyB;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.ChainOfResponsibility
 * @Description: TODO
 * @date Date : 2019年06月23日 17:36
 * 策略模式.
 */
public class Main {

    public static void main(String[] args) {
        Context context = new Context();
        context.setStrategy(new ConcreateStrategyA());
        context.algorithm();
        context.setStrategy(new ConcreateStrategyB());
        context.algorithm();
    }
}
