package org.java.learn.summary.java.framework.struct.decorator;

/**
 * Created by duyanlong on 2019/6/21.
 */
public class ManDecoratorA extends Decorator {

    @Override
    public void eating() {
        super.eating();
        reEating();
        System.out.println("执行 ManDecoratorA");
    }

    public void reEating(){
        System.out.println("再来一颗");
    }
}
