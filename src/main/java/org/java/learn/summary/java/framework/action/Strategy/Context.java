package org.java.learn.summary.java.framework.action.Strategy;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.Strategy
 * @Description: TODO
 * @date Date : 2019年07月01日 20:04
 */
public class Context {

    private IStrategy strategy;

    public IStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(IStrategy strategy) {
        this.strategy = strategy;
    }

    public void algorithm(){
        this.strategy.algorithm();
    }
}
