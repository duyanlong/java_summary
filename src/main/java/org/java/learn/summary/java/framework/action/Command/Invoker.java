package org.java.learn.summary.java.framework.action.Command;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.Command
 * @Description: TODO
 * @date Date : 2019年06月23日 19:19
 */
public class Invoker {

    private ICommand command = null;
    public  Invoker(ICommand command){
        this.command = command;
    }

    public void action(){
        command.execute();
    }

}
