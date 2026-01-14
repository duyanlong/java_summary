package org.java.learn.summary.java.framework.struct.flyweight;

/**
 * Created by duyanlong on 2019/6/20.
 * 享元模式.
 */
public class Main {

    public static void main(String[] args) {

        IFlyweight fly1 = FlyweightFactory.factory("key-a");
        fly1.operation("测试a");
        IFlyweight fly2 = FlyweightFactory.factory("key-b");
        fly2.operation("测试b");
        IFlyweight fly3 = FlyweightFactory.factory("key-c");
        fly3.operation("测试c");
        IFlyweight fly4 = FlyweightFactory.factory("key-d");
        fly4.operation("测试d");

        System.out.println("map size = " + FlyweightFactory.size());
    }
}
