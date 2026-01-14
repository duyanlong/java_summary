package org.java.learn.summary.java.framework.struct.decorator;

import org.java.learn.summary.java.framework.struct.decorator.impl.ManEatImpl;

/**
 * Created by duyanlong on 2019/6/20.
 * 装饰模式. TODO 适配器模式、装饰模式、外观模式、代理模式有些相似不好区分，需要总结区别在哪？
 */
public class Main {

    public static void main(String[] args) {
        IEat eat = new ManEatImpl();
        ManDecoratorA decoratorA = new ManDecoratorA();
        ManDecoratorB decoratorB = new ManDecoratorB();
        decoratorA.setEat(eat);
        decoratorB.setEat(eat);
        decoratorA.eating();
        System.out.println("========================");
        decoratorB.eating();
    }
}
