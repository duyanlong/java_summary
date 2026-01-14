package org.java.learn.summary.java.framework.create.abstract_factory;

import org.java.learn.summary.java.framework.create.abstract_factory.impl.BlackFactory;
import org.java.learn.summary.java.framework.create.abstract_factory.impl.WhiteFactory;

/**
 * Created by duyanlong on 2019/6/19.
 * 抽象工厂模式.
 */
public class Main {

    public static void main(String[] args) {
        IFactory whiteFactory = new WhiteFactory();
        whiteFactory.getCat().eating();
        whiteFactory.getDog().eating();

        IFactory blackFactory = new BlackFactory();
        blackFactory.getCat().eating();
        blackFactory.getDog().eating();
    }

}
