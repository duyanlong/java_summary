package org.java.learn.summary.spring.controller;

import java.util.Arrays;

/**
 * TODO.
 *
 * @author : duyanlong
 * @version V1.0
 * @Project: java_summary
 * @Package org.java.learn.summary.spring.controller
 * @date Date : 2020年11月06日 14:51
 */
public class ATest {

    public static void main(String[] args)
        throws ClassNotFoundException, InterruptedException, IllegalAccessException, InstantiationException {
        System.out.println("start-----");
        InvokeTestC cc = (InvokeTestC)Class.forName("org.java.learn.summary.spring.controller.InvokeTestC").newInstance();
        cc.aaa();
        Thread.sleep(1009);
        System.out.println("end-----");
    }

}
