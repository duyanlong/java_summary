package org.java.learn.summary.java.framework.action.Visitor;

import org.java.learn.summary.java.framework.action.Visitor.impl.Computer;
import org.java.learn.summary.java.framework.action.Visitor.impl.ComputerPartDisplayVisitor;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.ChainOfResponsibility
 * @Description: TODO
 * @date Date : 2019年06月23日 17:36
 * 访问者模式
 */
public class Main {

    public static void main(String[] args) {
        ComputerPart computer = new Computer();
        ComputerPartVisitor computerVisitor = new ComputerPartDisplayVisitor();
        computer.accept(computerVisitor);

    }
}
