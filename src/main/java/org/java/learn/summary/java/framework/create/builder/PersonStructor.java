package org.java.learn.summary.java.framework.create.builder;

/**
 * Created by duyanlong on 2019/6/19.
 */
public class PersonStructor {

    public Person structPerson(IBuilder iBuilder){
        iBuilder.buildHead();
        iBuilder.buildBody();
        iBuilder.buildFoot();
        return iBuilder.buildPerson();
    }
}
