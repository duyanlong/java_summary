package org.java.learn.summary.java.framework.action.Interpreter;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.ChainOfResponsibility
 * @Description: TODO
 * @date Date : 2019年06月23日 17:36
 * 解释器模式.
 */
public class Main {

    public static void main(String[] args) {

        Context context = new Context();
        context.freeRide("深圳的老人");
        context.freeRide("上海的年轻人");
        context.freeRide("北京的妇女");
        context.freeRide("广州的小孩");
        context.freeRide("山东的小孩");

    }
}
