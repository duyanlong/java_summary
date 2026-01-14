package org.java.learn.summary.java.framework.action.Iterator;

import org.java.learn.summary.java.framework.action.Iterator.impl.ListImpl;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.ChainOfResponsibility
 * @Description: TODO
 * @date Date : 2019年06月23日 17:36
 * 迭代器模式.
 */
public class Main {

    public static void main(String[] args) {
        List list = new ListImpl();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        // 第一种方式迭代方式
        Iterator itr = list.iterator();
        while(itr.hasNext()){
            System.out.println("iterator = " + itr.next());
        }

        System.out.println("=========================");
        System.out.println("=========================");

        // 第二种迭代方式
        for(int i = 0;i<list.getSize();i++){
            System.out.println("foreach = " + list.get(i));
        }
    }
}
