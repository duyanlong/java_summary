package org.java.learn.summary.java.framework.action.Memento;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.Memento
 * @Description: TODO
 * @date Date : 2019年06月25日 20:05
 */
public class Originator {
    private String state;

    public Memento createMemento(){
        return new Memento(state);
    }

    public void setMemento(Memento memento){
        state = memento.getState();
    }

    public void showState(){
        System.out.println(state);
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
