package org.java.learn.summary.java.framework.create.abstract_factory.impl;

import org.java.learn.summary.java.framework.create.abstract_factory.ICat;

/**
 * Created by duyanlong on 2019/6/19.
 */
public class BlackCat implements ICat {

    @Override
    public void eating() {
        System.out.println("the black cat is eating");
    }
}
