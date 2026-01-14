package org.java.learn.summary.java.framework.struct.composite.entity;

import java.util.List;

/**
 * Created by duyanlong on 2019/6/20.
 */
public class Employ {

    protected List<Employ> employs;
    protected String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void add(Employ employ){}
    public void delete(Employ employ){}
    public void printName(){
        System.out.println(name);
    }

    public List<Employ> getEmploys() {
        return employs;
    }
}
