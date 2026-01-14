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
public class Computer implements ComputerPart {
    ComputerPart[] parts;
    public Computer(){
        parts = new ComputerPart[]{new Mouse(),new Keyboard(),new Monitor()};
    }

    @Override
    public void accept(ComputerPartVisitor computerPartVisitor) {
        for(ComputerPart compart:parts){
            compart.accept(computerPartVisitor);
        }
        computerPartVisitor.visit(this);
    }
}
