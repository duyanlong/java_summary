package org.java.learn.summary.java.framework.action.Interpreter;

import org.java.learn.summary.java.framework.action.Interpreter.impl.AndExpression;
import org.java.learn.summary.java.framework.action.Interpreter.impl.TerminalExpression;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.Interpreter
 * @Description: TODO
 * @date Date : 2019年06月24日 12:08
 */
public class Context {
    private String[] citys = {"北京","上海","广州","深圳"};
    private String[] persons = {"老人","小孩","残疾人","孕妇"};
    private IExpression expression = null;

    public Context(){
        IExpression cityExp = new TerminalExpression(citys);
        IExpression personExp = new TerminalExpression(persons);
        expression = new AndExpression(cityExp,personExp);
    }

    /**
     * 调用相关表达式类的解释方法.
     * @param info
     */
    public void freeRide(String info){
        boolean ok = expression.interpret(info);
        if(ok){
            System.out.println("您是"+info+" ，您本次乘车免费！");
        }else{
            System.out.println(info+" ，您不是免费人员，本次乘车扣费2元！");
        }
    }

}
