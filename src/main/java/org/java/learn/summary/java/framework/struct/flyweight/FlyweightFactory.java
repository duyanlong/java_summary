package org.java.learn.summary.java.framework.struct.flyweight;

import org.java.learn.summary.java.framework.struct.flyweight.impl.ConcreateFlyweight;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package com.qihoo.net.bigdata.java.framework.struct.flyweight
 * @Description: TODO
 * @date Date : 2019年06月23日 16:32
 */
public class FlyweightFactory {

    private static Map<String,IFlyweight> flys = new HashMap();

    public static IFlyweight factory(String key){

        IFlyweight fly1 = flys.get(key);
        if(fly1==null){
            fly1 = new ConcreateFlyweight(key);
            flys.put(key,fly1);
        }
        return fly1;
    }

    public static int size(){
        return flys.size();
    }
}
