

## 背景
当我们需要将项目部署到其他环境部署时，大家应该都会面临修改properties文件以适应不同环境的配置问题；
这样来回更改配置是很麻烦的事情，为了解决这个问题，maven打包本身支持多环境切换，来解决大家的部署烦恼，具体使用方法我们看下面的步骤

## 修改配置
1、在xxx.pom文件中增加如下配置
```text
# 和build 标签同级增加
<profiles>
        <profile>
            <id>dev</id>
            <properties>
                <profiles.active>dev</profiles.active>
            </properties>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
        </profile>
        <profile>
            <id>test</id>
            <properties>
                <profiles.active>test</profiles.active>
            </properties>
        </profile>
        <profile>
            <id>prod</id>
            <properties>
                <profiles.active>prod</profiles.active>
            </properties>
        </profile>
    </profiles>

# build中增加
    <resources>
            <!-- 支持加载mybatis实体映射文件 -->
            <resource>
                <directory>src/main/java</directory>
                <includes>
                    <include>**/*.xml</include>
                </includes>
                <filtering>false</filtering>
            </resource>
            <!-- 支持加载resources目录下配置文件 -->
            <resource>
                <directory>src/main/resources</directory>
                <includes>
                    <include>*.properties</include>
                </includes>
                <excludes>
                    <exclude>env/*.properties</exclude>
                </excludes>
                <filtering>true</filtering>
            </resource>
    </resources>
    <!-- 过滤可用的替换配置 -->
        <filters>
            <filter>src/main/resources/env/${profiles.active}.properties</filter>
        </filters>
```

2、将properties中不同环境需要变化的变量提取到src/main/resources/env/[dev、test、prod].properties中
```text
jdbc.url=jdbc:mysql://localhost:3306/test_db?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull
jdbc.username=test
jdbc.password=test
jdbc.driver=com.mysql.jdbc.Driver
```

3、在spring的application.properties文件中引用环境变量进行配置
```text
spring.datasource.url=@jdbc.url@
spring.datasource.username=@jdbc.username@
spring.datasource.password=@jdbc.password@
spring.datasource.driver-class-name=@jdbc.driver@
```

## 结果
在maven打包时选择对应的profiles为dev、test、prod等生成的包中就直接替换为
mvn package -P test
jar xvf xxx.jar
cat BOOT-INFO/classes/application.properties

```text
spring.datasource.url=jdbc:mysql://localhost:3306/test_db?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull
spring.datasource.username=test
spring.datasource.password=test
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
```
