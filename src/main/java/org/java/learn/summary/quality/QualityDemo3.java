package org.java.learn.summary.quality;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.java.learn.summary.beans.CustomQuality;
import org.java.learn.summary.storage.OracleStorage;
import org.java.learn.summary.utils.HttpClientUtils;
import org.java.learn.summary.utils.QualityUtils;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import sun.misc.BASE64Encoder;

/**
 * Created by duyanlong on 2019/4/11.
 * 28所质量稽核自定义demo类
 */
public class QualityDemo3 {

    static int QUALIFIED_SCORE = 60;

    /**
     * 主函数，用于稽核军衔信息.
     *
     * @param args 参数
     * @throws IOException 异常
     * @throws URISyntaxException 异常
     */
    public static void main(String[] args) throws IOException, URISyntaxException {

        // 参数解析封装为实体类, 6个参数
        CustomQuality customQuality = QualityUtils.analysisParams(args);
        JSONObject customJson = customQuality.getParmJson();
        String mediaField = customJson.getString("mediaField");
        String relationField = customJson.getString("relationField");
        String mainJx = customJson.getString("jxField");

        // 获取数据库连接信息
        Map connectInfo = JSON.parseObject(customQuality.getConnInfo().toJSONString(), Map.class);
        OracleStorage oracleStorage = new OracleStorage(connectInfo);

        // 获取字段列表和字段类型
        Map<String, String> fieldMap = concatField(customQuality.getSchema(), relationField,
            mediaField);

        // 组装查询sql
        String selectSql = composeSql(customQuality, relationField, mediaField);

        // 直接关联查询，查出数据；
        Map<String, Map<String, Object>> slaveTable = getTableData(oracleStorage, fieldMap,
            selectSql, relationField);

        long sumScore = 0L;
        long idx = 0L;
        JSONObject paramJsonObject = new JSONObject();
        JSONArray mediaJsonArray = new JSONArray();
        paramJsonObject.put("data", mediaJsonArray);
        BASE64Encoder encoder = new BASE64Encoder();

        for (Map<String, Object> mediaInfo : slaveTable.values()) {
            // 组装识别媒体文件关键信息的请求参数  TODO 接口和当前字段不符合
            setMediaInfo(mediaJsonArray, encoder, mediaInfo, mediaField, relationField);

            // 每20条数据请求一次
            if (idx != 0 && (idx % 20 == 0 || idx + 1 == slaveTable.size())) {
                // 批量识别媒体文件中的关键信息
                paramJsonObject.put("taskId", String.format("%s_%s_%s_%s", "custom", customQuality
                    .getTableName(), customQuality.getThedate(), Math.round(idx / 20)));

                // 请求图像识别接口，由28所提供，mediaUrl为前端配置而来
                String result = HttpClientUtils.sendParamsPost(customJson.getString("mediaUrl"),
                    paramJsonObject);

                // 解析媒体媒体文件的关键信息，并与主表进行对比
                JSONObject mediaJsons = JSONObject.parseObject(result);
                JSONArray mediaArray = mediaJsons.getJSONArray("data");

                // 循环校对每条数据的军衔
                for (int j = 0; j < mediaArray.size(); j++) {
                    sumScore += computeScore(Integer.valueOf(mediaInfo.get(mainJx).toString()),
                        mediaArray
                            .getJSONObject(j));
                }
                paramJsonObject = new JSONObject();
                mediaJsonArray = new JSONArray();
                paramJsonObject.put("data", mediaJsonArray);
            }
            idx += 1;
        }

        if (idx == 0) {
            // 程序计算完成返回计算结果
            System.out.print(0);
        } else {
            // 程序计算完成返回计算结果
            System.out.print(sumScore / idx);
        }
    }

    private static String composeSql(CustomQuality customQuality, String relationField,
        String mediaField) {
        String mainTable = customQuality.getTableName();
        List<String> mainFields = customQuality.getSchema();
        StringBuffer selectSql = new StringBuffer("select ");
        String joinTable = customQuality.getRelationParams().getJSONObject(0)
            .getString("tableName");
        // 添加去重后的列
        for (String field : mainFields) {
            selectSql.append(String.format(" %s.%s,", mainTable, field));
        }
        selectSql.append(String.format("%s.%s", joinTable,mediaField));

        selectSql.append(" from ");
        selectSql.append(mainTable);
        selectSql.append(" left join ");
        selectSql.append(joinTable);
        selectSql.append(String.format(" on %s.%s = %s.%s ", mainTable, relationField,
            joinTable, relationField));

        return selectSql.toString();
    }

    private static Map<String, String> concatField(List<String> schema, String relationField, String
        mediaField) {
        Map<String, String> fieldMap = new HashMap<>();
        // 添加主表列
        for (String field1 : schema) {
            fieldMap.put(field1, "string");
        }

        // 添加关联列
        fieldMap.put(relationField, "string");

        // 添加媒体字段列
        fieldMap.put(mediaField, "blob");
        return fieldMap;
    }

    private static int computeScore(int mainJx, JSONObject mediaJson1) {
        int stat = mediaJson1.getInteger("stat");

        // 媒体文件识别失败，记60分.
        if (stat != 1) {
            int slaveJx = 0;
            try {
                if (mediaJson1.getJSONObject("result").containsKey("jx")) {
                    slaveJx = mediaJson1.getJSONObject("result").getInteger("jx");
                }
            } catch (Exception exp) {
                exp.getCause();
            }

            // 解析军衔信息失败则为60分
            if (slaveJx == 0) {
                return QUALIFIED_SCORE;
            } else if (mainJx == slaveJx) {
                // 识别的级别与主表中的信息一致，则记100分
                return 100;
            } else if (mainJx != slaveJx) {
                // 识别的级别与主表信息，相差一级减10分，相差两级减20分，以此类推，最低60分
                int tempScore = Math.max(100 - Math.abs(mainJx - slaveJx) * 10, 60);
                return Math.max(tempScore, QUALIFIED_SCORE);
            }
        }

        return QUALIFIED_SCORE;
    }

    private static void setMediaInfo(JSONArray mediaJsonArray, BASE64Encoder encoder,
        Map<String, Object> mediaInfo, String mediaField, String relationField) {
        // 组装识别媒体文件关键信息的请求参数
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("id", String.valueOf(mediaInfo.get(relationField)));
        jsonObject2.put("mediaCode", "");
        jsonObject2.put("mediaType", "");
        jsonObject2.put("mediaFormat", "");
        byte[] mediaByte = (byte[]) mediaInfo.get(mediaField);
        jsonObject2.put("mediaData", encoder.encode(mediaByte));
        mediaJsonArray.add(jsonObject2);
    }

    private static Map<String, Map<String, Object>> getTableData(OracleStorage oracleStorage,
        Map<String, String> fieldParamMap, String sql, String relationField) {

        List<Map<String, Object>> tmpList = oracleStorage.runSelect(sql, fieldParamMap);
        Map<String, Map<String, Object>> tmpMap = new HashMap<>();
        for (Map<String, Object> map1 : tmpList) {
            if (map1.get(relationField) != null) {
                tmpMap.put(map1.get(relationField).toString(), map1);
            }
        }
        return tmpMap;
    }

}
