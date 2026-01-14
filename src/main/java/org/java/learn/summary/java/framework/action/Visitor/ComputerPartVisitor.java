package org.java.learn.summary.java.framework.action.Visitor;

import org.java.learn.summary.java.framework.action.Visitor.impl.Computer;
import org.java.learn.summary.java.framework.action.Visitor.impl.Keyboard;
import org.java.learn.summary.java.framework.action.Visitor.impl.Monitor;
import org.java.learn.summary.java.framework.action.Visitor.impl.Mouse;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.Visitor
 * @Description: TODO
 * @date Date : 2019年07月02日 11:21
 */
public interface ComputerPartVisitor {

    void visit(Keyboard keyboard);
    void visit(Monitor monitor);
    void visit(Mouse mouse);
    void visit(Computer computer);
}
