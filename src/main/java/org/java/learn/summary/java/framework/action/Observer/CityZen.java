package org.java.learn.summary.java.framework.action.Observer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.Observer
 * @Description: TODO
 * @date Date : 2019年06月29日 2:20
 */
public abstract class CityZen {

    private List<PoliceMan> pols;
    private String help;

    public List<PoliceMan> getPols() {
        return pols;
    }

    public String getHelp() {
        return help;
    }

    public void setHelp(String help) {
        this.help = help;
    }

    public abstract void sendMessage(String help);
    public void setPolices(){
        pols = new ArrayList();
    }

    public void registerPolices(PoliceMan pol){
        pols.add(pol);
    }

}
