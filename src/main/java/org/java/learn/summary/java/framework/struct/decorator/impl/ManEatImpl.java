package org.java.learn.summary.java.framework.struct.decorator.impl;

import org.java.learn.summary.java.framework.struct.decorator.IEat;

/**
 * Created by duyanlong on 2019/6/21.
 */
public class ManEatImpl implements IEat{

    @Override
    public void eating() {
        System.out.println("吃一颗花生米");
    }
}
