package org.java.learn.summary.java.framework.create.builder;

/**
 * Created by duyanlong on 2019/6/19.
 */
public interface IBuilder {

    void buildHead();
    void buildBody();
    void buildFoot();
    Person buildPerson();
}
