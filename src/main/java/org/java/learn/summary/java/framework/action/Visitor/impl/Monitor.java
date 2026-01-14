package org.java.learn.summary.java.framework.action.Visitor.impl;

import org.java.learn.summary.java.framework.action.Visitor.ComputerPart;
import org.java.learn.summary.java.framework.action.Visitor.ComputerPartVisitor;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.Visitor.impl
 * @Description: TODO
 * @date Date : 2019年07月02日 11:23
 */
public class Monitor implements ComputerPart {

    @Override
    public void accept(ComputerPartVisitor computerPartVisitor) {
        computerPartVisitor.visit(this);
    }
}
