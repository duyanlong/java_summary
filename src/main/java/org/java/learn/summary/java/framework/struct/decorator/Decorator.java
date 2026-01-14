package org.java.learn.summary.java.framework.struct.decorator;

/**
 * Created by duyanlong on 2019/6/21.
 */
public abstract class Decorator implements IEat {

    protected IEat eat;
    public void setEat(IEat eat){
        this.eat = eat;
    }

    public void eating(){
        eat.eating();
    }
}
