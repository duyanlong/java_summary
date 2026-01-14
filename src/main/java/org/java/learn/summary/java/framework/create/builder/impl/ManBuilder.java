package org.java.learn.summary.java.framework.create.builder.impl;

import org.java.learn.summary.java.framework.create.builder.IBuilder;
import org.java.learn.summary.java.framework.create.builder.Man;
import org.java.learn.summary.java.framework.create.builder.Person;

/**
 * Created by duyanlong on 2019/6/19.
 */
public class ManBuilder implements IBuilder {

    Person person;

    public ManBuilder(){
        person = new Man();
    }

    @Override
    public void buildHead() {
        person.setHead("建造男人的头");
    }

    @Override
    public void buildBody() {
        person.setBody("建造男人的身体");
    }

    @Override
    public void buildFoot() {
        person.setFoot("建造男人的脚");
    }

    @Override
    public Person buildPerson() {
        return person;
    }
}
