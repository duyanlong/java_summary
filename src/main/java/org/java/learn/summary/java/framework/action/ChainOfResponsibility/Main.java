package org.java.learn.summary.java.framework.action.ChainOfResponsibility;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.ChainOfResponsibility
 * @Description: TODO
 * @date Date : 2019年06月23日 17:36
 * 责任链模式.
 */
public class Main {

    public static void main(String[] args) {

        AbstractLogger logger = ChainPattern.getChainLoggers();
        logger.logMessage(AbstractLogger.DEBUG,"debug");
        System.out.println("====================");
        logger.logMessage(AbstractLogger.INFO,"info");
        System.out.println("====================");
        logger.logMessage(AbstractLogger.ERROR,"error");
        System.out.println("====================");

    }
}
