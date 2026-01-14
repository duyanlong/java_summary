package org.java.learn.summary.beans;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by duyanlong on 2019/4/11.
 */
@Getter
@Setter
public class CustomQuality {

    /**
     * 质量规则id.
     */
    long qualityId;

    /**
     * 表名.
     */
    String tableName;

    /**
     * 作业计算日期.
     */
    String thedate;

    /**
     * 用户设置的自定义参数json.
     */
    JSONObject parmJson;

    /**
     * 数据库连接信息.
     */
    JSONObject connInfo;

    /**
     * 模型中的列信息.
     */
    List<String> schema;

    /**
     * 关联模型信息.
     */
    JSONArray relationParams;
}
