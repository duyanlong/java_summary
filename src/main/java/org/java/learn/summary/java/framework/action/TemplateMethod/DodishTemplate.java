package org.java.learn.summary.java.framework.action.TemplateMethod;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.TemplateMethod
 * @Description: TODO
 * @date Date : 2019年07月01日 20:15
 */
public abstract class DodishTemplate {

    public abstract void prepare();
    public abstract void doing();
    public abstract void clear();

    void dodish(){
        this.prepare();
        this.doing();
        this.clear();
    }
}
