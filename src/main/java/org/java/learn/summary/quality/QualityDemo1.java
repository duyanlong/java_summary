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
public class QualityDemo1 {

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
        // 获取数据库连接信息
        Map connectInfo = JSON.parseObject(customQuality.getConnInfo().toJSONString(), Map.class);
        OracleStorage oracleStorage = new OracleStorage(connectInfo);

        // 查询主表数据
        Map<String, Integer> mainMap = getMainTableData(oracleStorage);

        // 获取媒体文件信息
        Map<String, Map<String, Object>> slaveTable = getSlaveTableData(oracleStorage);

        long sumScore = 0L;
        long idx = 0L;
        int batchIdx = 0;
        JSONObject paramJsonObject = new JSONObject();
        JSONArray mediaJsonArray = new JSONArray();
        paramJsonObject.put("data", mediaJsonArray);
        BASE64Encoder encoder = new BASE64Encoder();
        // 循环对比信息
        for (Map.Entry<String, Integer> mainEntry : mainMap.entrySet()) {
            Map<String, Object> mediaInfo = slaveTable.get(mainEntry.getKey());
            // 组装识别媒体文件关键信息的请求参数
            setMediaInfo(mediaJsonArray, encoder, mediaInfo);

            // 每20条数据请求一次
            if (idx != 0 && (idx % 20 == 0 || idx + 1 == mainMap.size())) {
                // 批量识别媒体文件中的关键信息
                paramJsonObject.put("taskId", String.format("%s_%s_%s_%s", "custom", customQuality
                    .getTableName(), customQuality.getThedate(), batchIdx));

                // 请求图像识别接口，由28所提供，mediaUrl为前端配置而来
                String result = HttpClientUtils.sendParamsPost(customJson.getString("mediaUrl"),
                    paramJsonObject);

                // 解析媒体媒体文件的关键信息，并与主表进行对比
                JSONObject mediaJsons = JSONObject.parseObject(result);
                JSONArray mediaArray = mediaJsons.getJSONArray("data");

                // 循环校对每条数据的军衔
                for (int j = 0; j < mediaArray.size(); j++) {
                    JSONObject mediaJson1 = mediaArray.getJSONObject(j);
                    int stat = mediaJson1.getInteger("stat");
                    // 媒体文件识别失败，记60分.
                    if (stat == 1) {
                        sumScore += QUALIFIED_SCORE;
                    } else {
                        int slaveJx = 0;
                        try {
                            if (mediaJson1.getJSONObject("result").containsKey("jx")) {
                                slaveJx = mediaJson1.getJSONObject("result").getInteger("jx");
                            }
                        } catch (Exception exp) {
                            exp.getCause();
                        }
                        int mainJx = mainMap.get(mediaJson1.getString("id"));

                        // 解析军衔信息失败则为60分
                        if (slaveJx == 0) {
                            sumScore += QUALIFIED_SCORE;
                        } else if (mainJx == slaveJx) {
                            // 识别的级别与主表中的信息一致，则记100分
                            sumScore += 100;
                        } else if (mainJx != slaveJx) {
                            // 识别的级别与主表信息，相差一级减10分，相差两级减20分，以此类推，最低60分
                            int tempScore = Math.max(100 - (mainJx - slaveJx) * 10, 60);
                            sumScore += Math.max(tempScore, QUALIFIED_SCORE);
                        }
                    }
                }
                paramJsonObject = new JSONObject();
                mediaJsonArray = new JSONArray();
                paramJsonObject.put("data", mediaJsonArray);
                batchIdx++;
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

    private static void setMediaInfo(JSONArray mediaJsonArray, BASE64Encoder encoder,
        Map<String, Object> mediaInfo) {
        // 组装识别媒体文件关键信息的请求参数
        JSONObject jsonObject2 = new JSONObject();
        jsonObject2.put("id", String.valueOf(mediaInfo.get("sfzhm")));
        jsonObject2.put("mediaCode", String.valueOf(mediaInfo.get("mtnm")));
        jsonObject2.put("mediaType", String.valueOf(mediaInfo.get("mtlxnm")));
        jsonObject2.put("mediaFormat", String.valueOf(mediaInfo.get("mtgsnm")));
        byte[] mediaByte = (byte[]) mediaInfo.get("mtdx");
        jsonObject2.put("mediaData", encoder.encode(mediaByte));
        mediaJsonArray.add(jsonObject2);
    }

    private static Map<String, Map<String, Object>> getSlaveTableData(OracleStorage oracleStorage) {
        // 获取媒体文件信息
        Map<String, String> slaveParamMap = new HashMap<>();
        slaveParamMap.put("sfzhm", "string"); //唯一标识（身份证号码）
        slaveParamMap.put("mtnm", "string"); //媒体内码
        slaveParamMap.put("mtlxnm", "string"); //媒体类型内码
        slaveParamMap.put("mtgsnm", "string"); //媒体格式内码
        slaveParamMap.put("mtdx", "blob"); //媒体数据
        String slaveSql = "select sfzhm,mtnm,mtlxnm,mtgsnm,mtdx from ZZLL_GJGWRY_MT mt left join "
            + "ZZLL_GJGWRY_JBQK jbqk on mt.sfzhm = jbqk.sfzhm ";

        List<Map<String, Object>> tmpList = oracleStorage.runSelect(slaveSql, slaveParamMap);
        Map<String, Map<String, Object>> tmpMap = new HashMap<>();
        for (Map<String, Object> map1 : tmpList) {
            if (map1.get("sfzhm") != null) {
                tmpMap.put(map1.get("sfzhm").toString(), map1);
            }
        }
        return tmpMap;
    }

    private static Map<String, Integer> getMainTableData(OracleStorage oracleStorage) {
        // 查询主表数据
        String mainSql = "select sfzhm,jxnm from ZZLL_GJGWRY_JBQK ";
        Map<String, String> mainParamMap = new HashMap<>();
        mainParamMap.put("sfzhm", "string");
        mainParamMap.put("jxnm", "string");
        List<Map<String, Object>> mainTable = oracleStorage.runSelect(mainSql, mainParamMap);
        Map<String, Integer> mainMap = new HashMap<>();
        for (Map<String, Object> map1 : mainTable) {
            // 身份证号码或军衔内码为空则忽略
            if (map1.get("sfzhm") != null && !"".equals(map1.get("sfzhm")) && map1.get("jxnm") !=
                null && !"".equals(map1.get("jxnm"))) {
                mainMap.put(String.valueOf(map1.get("sfzhm")),
                    Integer.valueOf(map1.get("jxnm").toString()));
            }
        }
        return mainMap;
    }
}
