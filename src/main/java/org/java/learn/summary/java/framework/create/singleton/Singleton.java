package org.java.learn.summary.java.framework.create.singleton;

/**
 * Created by duyanlong on 2019/6/19.
 */
public class Singleton {

    private static Singleton singleton;

    private Singleton() {
    }

    public static Singleton getInstance() {
        if (singleton == null) {
            singleton = new Singleton();
        }
        return singleton;
    }
}
