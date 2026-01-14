package org.java.learn.summary.java.framework.action.Observer.impl;

import org.java.learn.summary.java.framework.action.Observer.PoliceMan;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.action.Observer.impl
 * @Description: TODO
 * @date Date : 2019年06月29日 2:33
 */
public class PoliceMan3 extends PoliceMan{

    @Override
    public void action(String message) {
        if("yes".equals(message)){
            System.out.println("接到报警，警察3出动！");
        }else{
            System.out.println("未接到报警，警察3在警局喝茶打牌！");
        }
    }
}
