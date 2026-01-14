**本文为mybatis代码自动生成配置使用方式**
**一定要根据自己需要的表执行，避免重新生成后覆盖人工修改部分**
* 1、pom.xml文件引入maven-mybatis插件
```xml
<!--Mybatis Generator-->
    <build>
        <plugins>
            <plugin>
                <groupId>org.mybatis.generator</groupId>
                <artifactId>mybatis-generator-maven-plugin</artifactId>
                <version>1.3.5</version>
                <dependencies>
                    <dependency>
                        <groupId>mysql</groupId>
                        <artifactId>mysql-connector-java</artifactId>
                        <version>5.1.47</version>
                    </dependency>
                    <dependency>
                        <groupId>org.mybatis.generator</groupId>
                        <artifactId>mybatis-generator-core</artifactId>
                        <version>1.3.5</version>
                    </dependency>
                </dependencies>
                <configuration>
                    <!--允许移动生成的文件 -->
                    <verbose>true</verbose>
                    <!-- 是否覆盖 -->
                    <overwrite>true</overwrite>
                    <!-- 自动生成的配置，这里制定对应位置的配置文件 -->
                    <configurationFile>
                        src/main/resources/generatorConfig.xml
                    </configurationFile>
                </configuration>
            </plugin>

        </plugins>
    </build>
```

* 2、在resources目录下创建generatorConfig.xml，自动生成代码配置文件，内容如下：
```xml
<!DOCTYPE generatorConfiguration
        PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
        "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">
 
<generatorConfiguration>
    <properties resource="config.properties"/>
 
    <context id="Mysql" targetRuntime="MyBatis3Simple" defaultModelType="flat">
        <property name="beginningDelimiter" value="`"/>
        <property name="endingDelimiter" value="`"/>
 
        <plugin type="tk.mybatis.mapper.generator.MapperPlugin">
            <property name="mappers" value="tk.mybatis.mapper.common.Mapper"/>
            <property name="caseSensitive" value="true"/>
        </plugin>
 
        <jdbcConnection driverClass="${jdbc.driverClass}"
                        connectionURL="${jdbc.url}"
                        userId="${jdbc.user}"
                        password="${jdbc.password}">
        </jdbcConnection>
 
        <javaModelGenerator targetPackage="com.isea533.mybatis.model" 
                            targetProject="src/main/java"/>
 
        <sqlMapGenerator targetPackage="mapper" 
                         targetProject="src/main/resources"/>
 
        <javaClientGenerator targetPackage="com.isea533.mybatis.mapper" 
                             targetProject="src/main/java"
                             type="XMLMAPPER"/>
 
          <!--domainObjectName：生成的domain类的名字，如果不设置，直接使用表名作为domain类的名字；可以设置为somepck.domainName，那么会自动把domainName类再放到somepck包里面；-->
                <!--enableInsert（默认true）：指定是否生成insert语句；-->
                <!--enableSelectByPrimaryKey（默认true）：指定是否生成按照主键查询对象的语句（就是getById或get）；-->
                <!--enableSelectByExample（默认true）：MyBatis3Simple为false，指定是否生成动态查询语句；-->
                <!--enableUpdateByPrimaryKey（默认true）：指定是否生成按照主键修改对象的语句（即update)；-->
                <!--enableDeleteByPrimaryKey（默认true）：指定是否生成按照主键删除对象的语句（即delete）；-->
                <!--enableDeleteByExample（默认true）：MyBatis3Simple为false，指定是否生成动态删除语句；-->
                <!--enableCountByExample（默认true）：MyBatis3Simple为false，指定是否生成动态查询总条数语句（用于分页的总条数查询）；-->
                <!--enableUpdateByExample（默认true）：MyBatis3Simple为false，指定是否生成动态修改语句（只修改对象中不为空的属性）；-->
                <table tableName="PGM_UDF_INFO" domainObjectName="PgmUdfInfo" mapperName="PgmUdfInfoMapper"
                       enableSelectByExample="false"
                       enableDeleteByExample="false"
                       enableCountByExample="false"
                       enableUpdateByExample="false" >
                    <generatedKey column="ID" sqlStatement="JDBC" />
                </table>
           
    </context>
</generatorConfiguration>
```
要生成的table，每增加一个，增加对应table标签元素即可；
其中jdbcConnection是用来配置要连接的表用的
javaModelGenerator用于配置生成的实体类存放位置
sqlMapGenerator用来存放*mapper.xml文件
javaClientGenerator用来存放Dao类路径


* 3、配置完成后，在maven tab中点击job-server -> Plugins -> mybatis-generator -> generate即可生成对应类
* 4、生成的类和配置文件如果不能完全满足你的要求，可自行修改生成文件