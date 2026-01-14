package org.java.learn.summary.java.framework.action.Memento;

import java.util.Arrays;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.ChainOfResponsibility
 * @Description: TODO
 * @date Date : 2019年06月23日 17:36
 * 备忘录模式.
 */
public class Main {

    public static void main(String[] args) {
        Originator org = new Originator();
        org.setState("第一次：开会中");

        Caretaker ctk = new Caretaker();
        ctk.setMemento(org.createMemento());

        org.setState("第二次：睡觉中");
        org.showState();

        // 将数据重新导入
        org.setMemento(ctk.getMemento());
        org.showState();
        System.out.println(" NullPointException");
    }
}
