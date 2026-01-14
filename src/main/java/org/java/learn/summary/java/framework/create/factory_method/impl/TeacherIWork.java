package org.java.learn.summary.java.framework.create.factory_method.impl;

import org.java.learn.summary.java.framework.create.factory_method.IWork;

/**
 * Created by duyanlong on 2019/6/19.
 */
public class TeacherIWork implements IWork {

    @Override
    public void doWork() {
        System.out.println("老师批改作业！");
    }
}
