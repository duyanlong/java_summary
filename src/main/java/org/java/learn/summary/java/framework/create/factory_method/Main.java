package org.java.learn.summary.java.framework.create.factory_method;

import org.java.learn.summary.java.framework.create.factory_method.impl.StudentIWorkFactory;
import org.java.learn.summary.java.framework.create.factory_method.impl.TeacherIWorkFactory;

/**
 * Created by duyanlong on 2019/6/19.
 * 工厂模式.
 */
public class Main {

    public static void main(String[] args) {

        IWorkFactory studentWorkFac = new StudentIWorkFactory();
        studentWorkFac.getWork().doWork();

        IWorkFactory teacherWorkFac = new TeacherIWorkFactory();
        teacherWorkFac.getWork().doWork();
    }
}
