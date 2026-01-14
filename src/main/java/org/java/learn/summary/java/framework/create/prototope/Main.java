package org.java.learn.summary.java.framework.create.prototope;

/**
 * Created by duyanlong on 2019/6/19.
 * 原型模式.
 */
public class Main {

    public static void main(String[] args) {
        Prototype prototype1 = new ConcreatePrototype("test1");
        Prototype prototype2 = (Prototype)prototype1.clone();

        System.out.println(prototype1.getName());
        System.out.println(prototype2.getName());
    }
}
