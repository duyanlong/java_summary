package org.java.learn.summary.java.framework.create.abstract_factory.impl;

import org.java.learn.summary.java.framework.create.abstract_factory.IDog;

/**
 * Created by duyanlong on 2019/6/19.
 */
public class WhiteDog implements IDog {

    @Override
    public void eating() {
        System.out.println("the white dog is eating");
    }
}
