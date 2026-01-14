package org.java.learn.summary.java.framework.action.Observer.impl;

import org.java.learn.summary.java.framework.action.Observer.CityZen;
import org.java.learn.summary.java.framework.action.Observer.PoliceMan;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.Observer.impl
 * @Description: TODO
 * @date Date : 2019年06月29日 2:26
 */
public class FengtaiCityZen extends CityZen{

    public FengtaiCityZen(){
        setPolices();
    }

    @Override
    public void sendMessage(String help) {
        for (PoliceMan pol1:getPols()){
            pol1.action(help);
        }
    }
}
