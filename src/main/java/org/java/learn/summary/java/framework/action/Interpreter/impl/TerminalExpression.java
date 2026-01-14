package org.java.learn.summary.java.framework.action.Interpreter.impl;

import org.java.learn.summary.java.framework.action.Interpreter.IExpression;
import java.util.HashSet;
import java.util.Set;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.Interpreter.impl
 * @Description: TODO
 * @date Date : 2019年06月24日 12:00
 */
public class TerminalExpression implements IExpression {

    private Set<String> set = new HashSet();
    public TerminalExpression(String[] data){
        for(int i = 0;i<data.length;i++){
            set.add(data[i]);
        }
    }

    /**
     * 对终结符表达式的处理
     * @param info
     * @return
     */
    @Override
    public boolean interpret(String info) {
        if(set.contains(info)){
            return true;
        }
        return false;
    }
}
