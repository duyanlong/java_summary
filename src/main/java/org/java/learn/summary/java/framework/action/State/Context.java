package org.java.learn.summary.java.framework.action.State;

import org.java.learn.summary.java.framework.action.State.impl.ConcreateStateA;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.State
 * @Description: TODO
 * @date Date : 2019年07月01日 19:48
 */
public class Context {

    private State state;
    public Context(){
        this.state = new ConcreateStateA();
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void handle(){
        state.handle(this);
    }
}
