package org.java.learn.summary.java.framework.create.abstract_factory.impl;

import org.java.learn.summary.java.framework.create.abstract_factory.ICat;
import org.java.learn.summary.java.framework.create.abstract_factory.IDog;
import org.java.learn.summary.java.framework.create.abstract_factory.IFactory;

/**
 * Created by duyanlong on 2019/6/19.
 */
public class BlackFactory implements IFactory {

    @Override
    public ICat getCat() {
        return new BlackCat();
    }

    @Override
    public IDog getDog() {
        return new BlackDog();
    }
}
