package org.java.learn.summary.java.framework.struct.decorator;

/**
 * Created by duyanlong on 2019/6/21.
 */
public class ManDecoratorB extends Decorator {

    public void eating(){
        super.eating();
        System.out.println("调用 ManDecoratorB");
    }
}
