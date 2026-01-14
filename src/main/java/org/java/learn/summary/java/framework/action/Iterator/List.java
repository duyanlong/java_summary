package org.java.learn.summary.java.framework.action.Iterator;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.Iterator
 * @Description: TODO
 * @date Date : 2019年06月24日 15:06
 */
public interface List {

    Iterator iterator();
    Object get(int index);
    int getSize();
    void add(Object object);
}
