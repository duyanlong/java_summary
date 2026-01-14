package org.java.learn.summary.java.framework.struct.composite;

import org.java.learn.summary.java.framework.struct.composite.entity.Employ;
import org.java.learn.summary.java.framework.struct.composite.entity.Programer;
import org.java.learn.summary.java.framework.struct.composite.entity.ProjectManager;

/**
 * Created by duyanlong on 2019/6/20.
 * 组合模式.
 */
public class Main {

    public static void main(String[] args) {
        Employ employ1 = new Programer();
        employ1.setName("程序员1");
        Employ employ2 = new Programer();
        employ2.setName("程序员2");

        Employ pManager = new ProjectManager();
        pManager.add(employ1);
        pManager.add(employ2);

        for (Employ em1:pManager.getEmploys()){
            em1.printName();
        }
    }
}
