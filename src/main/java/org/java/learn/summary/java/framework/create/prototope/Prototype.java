package org.java.learn.summary.java.framework.create.prototope;

/**
 * Created by duyanlong on 2019/6/20.
 */
public class Prototype implements Cloneable{

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected Object clone() {
        try {
            return super.clone();
        }catch (Exception exp){
            return null;
        }
    }
}
