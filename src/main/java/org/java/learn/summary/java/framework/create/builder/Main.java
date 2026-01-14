package org.java.learn.summary.java.framework.create.builder;

import org.java.learn.summary.java.framework.create.builder.impl.ManBuilder;

/**
 * Created by duyanlong on 2019/6/19.
 * 建造者模式.
 */
public class Main {

    public static void main(String[] args) {
        IBuilder iBuilder = new ManBuilder();
        PersonStructor structor = new PersonStructor();
        Person person = structor.structPerson(iBuilder);
        System.out.println(person.getHead());
        System.out.println(person.getBody());
        System.out.println(person.getFoot());
    }
}
