package org.java.learn.summary.java.framework.action.Visitor;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.Visitor
 * @Description: TODO
 * @date Date : 2019年07月02日 11:22
 */
public interface ComputerPart {

    void accept(ComputerPartVisitor computerPartVisitor);
}
