package org.java.learn.summary.java.framework.action.Visitor.impl;

import org.java.learn.summary.java.framework.action.Visitor.ComputerPartVisitor;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.Visitor.impl
 * @Description: TODO
 * @date Date : 2019年07月02日 11:28
 */
public class ComputerPartDisplayVisitor implements ComputerPartVisitor {

    @Override
    public void visit(Keyboard keyboard) {
        System.out.println("Displaying Keyboard");
    }

    @Override
    public void visit(Monitor monitor) {
        System.out.println("Displaying Monitor");
    }

    @Override
    public void visit(Mouse mouse) {
        System.out.println("Displaying Mouse");
    }

    @Override
    public void visit(Computer computer) {
        System.out.println("Displaying Computer");
    }
}
