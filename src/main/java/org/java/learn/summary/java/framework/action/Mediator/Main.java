package org.java.learn.summary.java.framework.action.Mediator;

import org.java.learn.summary.java.framework.action.Mediator.impl.Colleague1;
import org.java.learn.summary.java.framework.action.Mediator.impl.Colleague2;
import org.java.learn.summary.java.framework.action.Mediator.impl.ConcreateMediator;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.ChainOfResponsibility
 * @Description: TODO
 * @date Date : 2019年06月23日 17:36
 * 中介者模式.
 */
public class Main {

    public static void main(String[] args) {
        Mediator mediator = new ConcreateMediator();
        Colleague colleague1 = new Colleague1(mediator);
        Colleague colleague2 = new Colleague2(mediator);
        mediator.setColleague1(colleague1);
        mediator.setColleague2(colleague2);
        colleague1.send("最近还好吗？");
        colleague2.send("挺好的就是有点忙");
        colleague1.send("平常加班吗");
        colleague2.send("加啊，9106工作制");

    }
}
