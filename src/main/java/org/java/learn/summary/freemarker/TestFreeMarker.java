package org.java.learn.summary.freemarker;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 测试freemarker程序.
 * 参考官网： https://freemarker.apache.org/docs/dgui_quickstart_template.html .
 * @author yanlongdu
 * @date 2023/03/15
 */
public class TestFreeMarker {

    public static void main(String[] args) throws IOException, TemplateException {

        //1.创建配置类
        Configuration configuration = new Configuration(Configuration.getVersion());
        //2.设置模板所在的目录
        configuration.setDirectoryForTemplateLoading(new File("/Users/yanlongdu/code/org_java/java_summary/src/main/resources/tfl/"));
        //3.设置字符集
        configuration.setDefaultEncoding("utf-8");
//        select {} from {}.{}  where {}
//        regex.replace()
        //4.加载模板                                    模板名自动在模板所在目录寻找
        Template template = configuration.getTemplate("test.ftl");
        //5.创建数据模型
        Map map = new HashMap();
        map.put("databaseName", "样例库");
        map.put("tableName", "样例表1");
        map.put("columnName", "样例表名字段");
        map.put("ds", "2023-03-15 12:12:12");
        //6.创建Writer对象                   生成的静态资源的地址
//        Writer out = new FileWriter(new File("d:\\test.html"));
        Writer out = new OutputStreamWriter(System.out);
        System.out.println("=======================");
        System.out.println("=======================");
        System.out.println("=======================");
        System.out.println("=======================");
        //7.输出 开始生成
        try {
            template.process(map, out);
        } catch (Exception exp) {
            exp.printStackTrace();
        }

        System.out.println("=======================");
        System.out.println("=======================");
        System.out.println("=======================");
        System.out.println("=======================");
        //8.关闭Writer对象
    }
}
