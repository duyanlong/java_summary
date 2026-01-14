package org.java.learn.summary.java.framework.action.State.impl;

import org.java.learn.summary.java.framework.action.State.Context;
import org.java.learn.summary.java.framework.action.State.State;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.State.impl
 * @Description: TODO
 * @date Date : 2019年07月01日 19:50
 */
public class ConcreateStateA extends State {

    @Override
    public void handle(Context context) {
        System.out.println("当前状态A");
        context.setState(new ConcreateStateB());
    }
}
