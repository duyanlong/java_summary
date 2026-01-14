package org.java.learn.summary.java.framework.create.singleton;

/**
 * Created by duyanlong on 2019/6/19.
 * 单例模式 | 单态模式.
 */
public class Main {

    public static void main(String[] args) {

        Singleton sing1 = Singleton.getInstance();
        Singleton sing2 = Singleton.getInstance();

        System.out.println(sing1);
        System.out.println(sing2);
    }
}
