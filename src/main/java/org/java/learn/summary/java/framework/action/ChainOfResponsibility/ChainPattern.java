package org.java.learn.summary.java.framework.action.ChainOfResponsibility;

import org.java.learn.summary.java.framework.action.ChainOfResponsibility.impl.ErrorLogger;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.ChainOfResponsibility
 * @Description: TODO
 * @date Date : 2019年06月23日 18:52
 */
public class ChainPattern {

    public static AbstractLogger getChainLoggers(){
        AbstractLogger debuglogger = new ErrorLogger(AbstractLogger.DEBUG);
        AbstractLogger filelogger = new ErrorLogger(AbstractLogger.INFO);
        AbstractLogger errlogger = new ErrorLogger(AbstractLogger.ERROR);

        debuglogger.setNextLogger(filelogger);
        filelogger.setNextLogger(errlogger);
        return debuglogger;
    }

}
