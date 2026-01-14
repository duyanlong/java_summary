package org.java.learn.summary.java.framework.action.Iterator.impl;

import org.java.learn.summary.java.framework.action.Iterator.Iterator;
import org.java.learn.summary.java.framework.action.Iterator.List;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.Iterator.impl
 * @Description: TODO
 * @date Date : 2019年06月24日 15:13
 */
public class ListImpl implements List {

    private Object[] list;
    private int index;
    private int size;

    public ListImpl(){
        index = 0;
        size = 0;
        list = new Object[100];
    }

    @Override
    public Iterator iterator() {
        return new IteratorImpl(this);
    }

    @Override
    public Object get(int index) {
        return list[index];
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public void add(Object object) {

        list[index++] = object;
        size++;
    }
}
