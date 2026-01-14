package org.java.learn.summary.java.framework.action.Observer;

import org.java.learn.summary.java.framework.action.Observer.impl.ChaoyangCityZen;
import org.java.learn.summary.java.framework.action.Observer.impl.FengtaiCityZen;
import org.java.learn.summary.java.framework.action.Observer.impl.PoliceMan1;
import org.java.learn.summary.java.framework.action.Observer.impl.PoliceMan2;
import org.java.learn.summary.java.framework.action.Observer.impl.PoliceMan3;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.ChainOfResponsibility
 * @Description: TODO
 * @date Date : 2019年06月23日 17:36
 * 观察者模式.
 */
public class Main {

    public static void main(String[] args) {
        PoliceMan pol1 = new PoliceMan1();
        PoliceMan pol2 = new PoliceMan2();
        PoliceMan pol3 = new PoliceMan3();

        CityZen chaoyang = new ChaoyangCityZen();
        chaoyang.registerPolices(pol1);
        chaoyang.registerPolices(pol2);
        chaoyang.registerPolices(pol3);
        chaoyang.sendMessage("yes");

        System.out.println("==================");
        CityZen fengtai = new FengtaiCityZen();
        fengtai.registerPolices(pol1);
        fengtai.sendMessage("no");
    }
}
