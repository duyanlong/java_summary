package org.java.learn.summary.java.framework.struct.facade;

/**
 * Created by duyanlong on 2019/6/20.
 * 外观模式.
 */
public class Main {

    public static void main(String[] args) {
        Facade facade = new Facade();
        facade.start();
        System.out.println("=========================");
        facade.stop();
    }
}
