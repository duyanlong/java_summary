package org.java.learn.summary.java.framework.struct.facade;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.struct.facade
 * @Description: TODO
 * @date Date : 2019年06月23日 16:04
 */
public class Facade {

    private Cpu cpu = new Cpu();
    private Memory memory = new Memory();

    public void start(){
        cpu.start();
        memory.start();
    }

    public void stop(){
        cpu.stop();
        memory.stop();
    }
}
