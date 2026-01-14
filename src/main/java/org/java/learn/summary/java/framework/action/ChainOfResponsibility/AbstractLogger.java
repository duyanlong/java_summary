package org.java.learn.summary.java.framework.action.ChainOfResponsibility;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.ChainOfResponsibility
 * @Description: TODO
 * @date Date : 2019年06月23日 18:41
 */
public abstract class AbstractLogger {

    public static int ERROR = 1;
    public static int INFO = 2;
    public static int DEBUG = 3;

    protected int level;

    protected AbstractLogger nextLogger;

    public void setNextLogger(AbstractLogger nextLogger){
        this.nextLogger = nextLogger;
    }

    public void logMessage(int level,String message){
        if(this.level <= level){
            write(message);
        }

        if(nextLogger != null){
            nextLogger.logMessage(level,message);
        }
    }

    protected abstract void write(String message);

}
