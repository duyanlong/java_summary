package org.java.learn.summary.utils;

import com.alibaba.fastjson.JSON;
import org.java.learn.summary.beans.CustomQuality;
import java.io.IOException;
import java.util.Arrays;
import sun.misc.BASE64Decoder;

/**
 * Created by duyanlong on 2019/4/11.
 */
public class QualityUtils {

    /**
     * 解析主函数参数列表为质量稽核自定义算子可用参数
     */
    public static CustomQuality analysisParams(String[] args) {
        CustomQuality customQuality = new CustomQuality();

        if (args.length == 7) {

            customQuality.setQualityId(Long.valueOf(args[0]));
            customQuality.setTableName(args[1]);
            customQuality.setThedate(args[2]);
            customQuality.setParmJson(JSON.parseObject(args[3]));
            customQuality.setConnInfo(JSON.parseObject(args[4]));
            customQuality.setSchema(Arrays.asList(args[5].split(",")));
            try {
                BASE64Decoder decoder = new BASE64Decoder();
                customQuality.setRelationParams(
                    JSON.parseArray(new String(decoder.decodeBuffer(args[6]), "UTF-8")));
            } catch (IOException supportedExp) {
                supportedExp.printStackTrace();
            }
        }

        return customQuality;
    }

}
