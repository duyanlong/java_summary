package org.java.learn.summary.java.framework.action.TemplateMethod;

import org.java.learn.summary.java.framework.action.TemplateMethod.impl.DodishA;
import org.java.learn.summary.java.framework.action.TemplateMethod.impl.DodishB;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.ChainOfResponsibility
 * @Description: TODO
 * @date Date : 2019年06月23日 17:36
 * 模板方法.
 */
public class Main {

    public static void main(String[] args) {
        DodishTemplate dish1 = new DodishA();
        dish1.dodish();
        System.out.println("-------------------------");
        System.out.println("-------------------------");
        DodishTemplate dish2 = new DodishB();
        dish2.dodish();
    }
}
