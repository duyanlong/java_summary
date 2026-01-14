package org.java.learn.summary.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

/**
 * HttpClient工具类.
 * Created by duyanlong on 2018/12/28.
 */
public class HttpClientUtils {

    // private static Logger logger = LoggerFactory.getLogger(HttpClientUtils.class);

    // 默认字符集
    private static final String ENCODING = "UTF-8";

    /**
     * 发送post请求.
     *
     * @param url      请求地址.
     * @param headers  请求头.
     * @param jsonData 请求实体.
     * @param encoding 字符集.
     * @return String.
     */
    public static String sendPost(String url, Map<String, String> headers, JSONObject jsonData, String encoding)
            throws IOException {
        // logger.info(String.format("进入post请求方法 url: %s", url));
        // 请求返回结果
        String resultJson;
        // 创建Client
        HttpClient client = new DefaultHttpClient();
        // 创建HttpPost对象
        HttpPost httpPost = new HttpPost(url);

        try {
            // 设置请求头
            if (headers != null) {
                Header[] allHeader = new BasicHeader[headers.size()];
                int idx = 0;
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    allHeader[idx] = new BasicHeader(entry.getKey(), entry.getValue());
                    idx++;
                }
                httpPost.setHeaders(allHeader);
            }

            // 设置实体
            StringEntity entity = new StringEntity(jsonData.toString(), Charset.forName("UTF-8"));
            entity.setContentType(ContentType.APPLICATION_JSON.toString());
            httpPost.setEntity(entity);

            // 发送请求,返回响应对象
            HttpResponse response = client.execute(httpPost);
            // 获取响应状态
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                // 获取响应结果
                resultJson = EntityUtils.toString(response.getEntity(), encoding);
            } else {
                // logger.error(String.format("响应失败，状态码：%s", status));
                throw new IOException(String.format("响应失败，状态码：%s", status));
            }
        } catch (Exception ex) {
            // logger.error("发送post请求失败", ex);
            throw ex;
        } finally {
            httpPost.releaseConnection();
        }
        return resultJson;
    }

    /**
     * 发送post请求，请求数据默认使用json格式，默认使用UTF-8编码.
     *
     * @param url      请求地址
     * @param jsonData 请求实体
     * @return String
     */
    public static String sendJsonPost(String url, JSONObject jsonData) throws IOException {
        Map<String, String> headers = new HashMap<>();
        return sendPost(url, headers, jsonData, ENCODING);
    }

    /**
     * 发送post请求，请求数据默认使用UTF-8编码.
     *
     * @param url     请求地址
     * @param headers 请求头
     * @param params  请求实体
     * @return String
     */
    public static String sendParamsPost(String url, Map<String, String> headers, Map<String, String> params)
            throws IOException {
        // 将map转成json
        JSONObject data = JSONObject.parseObject(JSON.toJSONString(params));
        return sendPost(url, headers, data, ENCODING);
    }

    /**
     * 发送post请求，请求数据默认使用UTF-8编码.
     */
    public static String sendParamsPost(String url, JSONObject json) throws IOException {
        Map<String, String> headers = new HashMap<>();
        return sendPost(url, headers, json, ENCODING);
    }

    /**
     * 发送put请求.
     */
    public static String sendPut(String url, Map<String, String> headers, JSONObject jsonData, String encoding)
            throws IOException {
        // logger.info(String.format("进入put请求方法 url: %s", url));
        // 请求返回结果
        String resultJson;
        // 创建Client
        HttpClient client = new DefaultHttpClient();
        // 创建HttpPost对象
        HttpPut httpPut = new HttpPut(url);
        try {
            // 设置实体
            StringEntity entity = new StringEntity(jsonData.toString(), Charset.forName("UTF-8"));
            entity.setContentType(ContentType.APPLICATION_JSON.toString());
            httpPut.setEntity(entity);

            // 发送请求,返回响应对象
            HttpResponse response = client.execute(httpPut);
            // 获取响应状态
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                // 获取响应结果
                resultJson = EntityUtils.toString(response.getEntity(), encoding);
            } else {
                // logger.error(String.format("响应失败，状态码：%s", status));
                throw new IOException(String.format("响应失败，状态码：%s", status));
            }
        } catch (Exception ex) {
            // logger.error("发送post请求失败", ex);
            throw ex;
        } finally {
            httpPut.releaseConnection();
        }
        return resultJson;
    }

    /**
     * 发送put请求，请求数据默认使用UTF-8编码.
     */
    public static String sendParamsPut(String url, JSONObject json) throws IOException {
        Map<String, String> headers = new HashMap<>();
        return sendPut(url, headers, json, ENCODING);
    }

    /**
     * 发送post请求，url.
     */
    public static String sendKvPost(String url, List<NameValuePair> params) throws IOException {
        // logger.info(String.format("进入post请求方法 url: %s", url));
        // 请求返回结果
        String resultJson;
        // 创建Client
        HttpClient client = new DefaultHttpClient();
        // 创建HttpPost对象
        HttpPost httpPost = new HttpPost(url);
        try {
            // 设置实体
            HttpEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
            httpPost.setEntity(entity);

            // 发送请求,返回响应对象
            HttpResponse response = client.execute(httpPost);
            // 获取响应状态
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                // 获取响应结果
                resultJson = EntityUtils.toString(response.getEntity(), ENCODING);
            } else {
                // logger.error(String.format("响应失败，状态码：%s", status));
                throw new IOException(String.format("响应失败，状态码：%s", status));
            }
        } catch (Exception ex) {
            // logger.error("发送post请求失败", ex);
            throw ex;
        } finally {
            httpPost.releaseConnection();
        }
        return resultJson;
    }

    /**
     * 发送get请求.
     *
     * @param url      请求地址
     * @param params   请求参数
     * @param encoding 编码
     * @return String
     */
    public static String sendGet(String url, Map<String, Object> params, String encoding)
            throws IOException, URISyntaxException {

        // 请求结果
        String resultJson = null;
        // 创建client
        CloseableHttpClient client = HttpClients.createDefault();
        // 创建HttpGet
        HttpGet httpGet = new HttpGet();
        try {
            URIBuilder builder = new URIBuilder(url);
            // 封装参数
            if (params != null) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    builder.addParameter(entry.getKey(), entry.getValue().toString());
                }
            }
            URI uri = builder.build();
            // logger.info(String.format("进行get请求：url: %s ", uri));
            // 设置请求地址
            httpGet.setURI(uri);
            // 发送请求，返回响应对象
            CloseableHttpResponse response = client.execute(httpGet);
            // 获取响应状态
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                // 获取响应数据
                resultJson = EntityUtils.toString(response.getEntity(), encoding);
            } else {
                // logger.error("响应失败，状态码：", status);
            }
        } catch (Exception exp) {
            // logger.error("发送get请求失败", exp);
            throw exp;
        } finally {
            httpGet.releaseConnection();
        }
        return resultJson;
    }

    /**
     * 发送get请求.
     *
     * @param url    请求地址
     * @param params 请求参数
     * @return String
     */
    public static String sendGet(String url, Map<String, Object> params) throws IOException, URISyntaxException {
        return sendGet(url, params, ENCODING);
    }

    /**
     * 发送get请求.
     *
     * @param url 请求地址
     * @return String
     */
    public static String sendGet(String url) throws IOException, URISyntaxException {
        return sendGet(url, null, ENCODING);
    }
}
