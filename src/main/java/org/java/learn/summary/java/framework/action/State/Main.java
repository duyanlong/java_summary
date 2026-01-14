package org.java.learn.summary.java.framework.action.State;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.ChainOfResponsibility
 * @Description: TODO
 * @date Date : 2019年06月23日 17:36
 * 状态模式.
 */
public class Main {

    public static void main(String[] args) {
        Context context = new Context();
        context.handle();
        context.handle();
        context.handle();
        context.handle();
        context.handle();
    }
}
