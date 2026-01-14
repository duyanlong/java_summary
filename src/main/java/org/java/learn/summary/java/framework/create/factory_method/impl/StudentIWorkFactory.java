package org.java.learn.summary.java.framework.create.factory_method.impl;

import org.java.learn.summary.java.framework.create.factory_method.IWork;
import org.java.learn.summary.java.framework.create.factory_method.IWorkFactory;

/**
 * Created by duyanlong on 2019/6/19.
 */
public class StudentIWorkFactory implements IWorkFactory {

    @Override
    public IWork getWork() {
        return new StudentIWork();
    }
}
