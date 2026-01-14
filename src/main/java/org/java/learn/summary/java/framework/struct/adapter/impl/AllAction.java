package org.java.learn.summary.java.framework.struct.adapter.impl;

import org.java.learn.summary.java.framework.struct.adapter.IAction;

/**
 * Created by duyanlong on 2019/6/20.
 */
public class AllAction implements IAction {

    OpenAction openAction;
    public AllAction(OpenAction openAction){
        this.openAction = openAction;
    }

    @Override
    public void open() {
        openAction.open();
    }

    @Override
    public void close() {
        System.out.println("关闭大门");
    }
}
