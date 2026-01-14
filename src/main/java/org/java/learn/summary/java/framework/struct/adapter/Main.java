package org.java.learn.summary.java.framework.struct.adapter;

import org.java.learn.summary.java.framework.struct.adapter.impl.AllAction;
import org.java.learn.summary.java.framework.struct.adapter.impl.OpenAction;

/**
 * Created by duyanlong on 2019/6/20.
 * 适配器模式.
 */
public class Main {

    public static void main(String[] args) {
        IAction action = new AllAction(new OpenAction());
        action.open();
        action.close();
    }
}
