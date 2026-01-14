package org.java.learn.summary.java.framework.action.Iterator.impl;

import org.java.learn.summary.java.framework.action.Iterator.Iterator;
import org.java.learn.summary.java.framework.action.Iterator.List;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.Iterator.impl
 * @Description: TODO
 * @date Date : 2019年06月24日 15:05
 */
public class IteratorImpl implements Iterator {

    private List list;
    private int index;

    public IteratorImpl(List list){
        index = 0;
        this.list = list;
    }

    @Override
    public Object next() {

        Object obj = list.get(index);
        index++;
        return obj;
    }

    @Override
    public void first() {
        index = 0;
    }

    @Override
    public void last() {
        index = list.getSize();
    }

    @Override
    public boolean hasNext() {
        return index < list.getSize();
    }
}
