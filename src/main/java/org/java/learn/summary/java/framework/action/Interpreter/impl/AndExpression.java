package org.java.learn.summary.java.framework.action.Interpreter.impl;

import org.java.learn.summary.java.framework.action.Interpreter.IExpression;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.Interpreter.impl
 * @Description: TODO
 * @date Date : 2019年06月24日 12:04
 */
public class AndExpression implements IExpression {

    private IExpression city = null;
    private IExpression person = null;

    public AndExpression(IExpression city,IExpression person){
        this.city = city;
        this.person = person;
    }

    @Override
    public boolean interpret(String info) {
        String strs[] = info.split("的");
        //判断city是否包含存入的信息 && person是否包含存入的信息
        return city.interpret(strs[0]) && person.interpret(strs[1]);
    }
}
