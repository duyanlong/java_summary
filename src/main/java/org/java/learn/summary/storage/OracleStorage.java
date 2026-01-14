package org.java.learn.summary.storage;

import org.java.learn.summary.utils.DbUtils;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OracleStorage extends StorageStrategy {

    private static String driverName = "oracle.jdbc.driver.OracleDriver";

    private static Map connectInfo = null;

    public OracleStorage() {
    }

    /**
     * 初始化.
     */
    public OracleStorage(Map connectInfo) {
        if (null == this.connectInfo) {
            this.connectInfo = connectInfo;
        }
    }

    /**
     * 数据库连接.
     */
    public static Connection connection(Map<String, String> dbConnect)
        throws SQLException, ClassNotFoundException {
        String ip = dbConnect.get("ip");
        String port = dbConnect.get("port");
        String databaseName = dbConnect.get("dbname");
        String account = dbConnect.get("username");
        String password = dbConnect.get("password");

        String dburl = "jdbc:oracle:thin:@" + ip + ":" + port + ":" + databaseName;
        DbUtils dbUtils = new DbUtils();
        return dbUtils.registerDriver(driverName).getConnection(dburl, account, password);
    }

    /**
     * 执行更新 execmd.
     */
    public static Boolean runUpdate(Map map, String sql) throws SQLException {
        String ip = map.get("ip").toString();
        String port = map.get("port").toString();
        String databaseName = map.get("dbname").toString();
        String user = map.get("username").toString();
        String password = map.get("password").toString();
        String connectStr = "jdbc:oracle:thin:@" + ip + ":" + port + ":" + databaseName;

        DbUtils dbUtils = new DbUtils();

        Boolean flag = dbUtils.registerDriver(driverName)
            .connectionNoAutoTran(connectStr, user, password)
            .executeUpdateNoAutoTran(sql) > 0 ? true : false;
        dbUtils.closeDb();
        return flag;
    }

    /**
     * 执行查询 exescan.
     */
    public static List<String> runSelect(Map map, String sql, String column)
        throws SQLException {
        String ip = map.get("ip").toString();
        String port = map.get("port").toString();
        String databaseName = map.get("dbname").toString();
        String user = map.get("username").toString();
        String password = map.get("password").toString();
        String connectStr = "jdbc:oracle:thin:@" + ip + ":" + port + ":" + databaseName;

        DbUtils dbUtils = new DbUtils();
        ResultSet res = dbUtils.registerDriver(driverName)
            .connection(connectStr, user, password).executeQuery(sql);

        List<String> results = new ArrayList<>();
        while (res.next()) {
            String result = res.getString(column);
            results.add(result);
        }
        return results;
    }

    /**
     * 执行查询 exescan tmx.
     */
    public List<Map<String, Object>> runSelect(String sql, Map<String, String> columns) {
        String connectStr = "jdbc:oracle:thin:@" + connectInfo.get("ip").toString()
            + ":" + connectInfo.get("port").toString() + ":" + connectInfo.get("dbname").toString();

        DbUtils dbUtils = new DbUtils();
        String user = connectInfo.get("username").toString();
        String password = connectInfo.get("password").toString();
        ResultSet res = null;
        try {
            res = dbUtils.registerDriver(driverName).connection(connectStr, user, password)
                .executeQuery(sql);
        } catch (SQLException exp) {
            exp.printStackTrace();
        }

        List<Map<String, Object>> results = new ArrayList<>();
        if (res == null) {
            return results;
        }
        try {
            while (res.next()) {
                Map<String, Object> records = new HashMap<>();
                for (Map.Entry<String, String> column : columns.entrySet()) {
                    if ("string".equals(column.getValue())) {
                        records.put(column.getKey(), res.getString(column.getKey()));
                    } else if ("date".equals(column.getValue())) {
                        records.put(column.getKey(), res.getDate(column.getKey()));
                    } else if ("int".equals(column.getValue())) {
                        records.put(column.getKey(), res.getInt(column.getKey()));
                    } else if ("double".equals(column.getValue())) {
                        records.put(column.getKey(), res.getDouble(column.getKey()));
                    } else if ("blob".equals(column.getValue())) {
                        records.put(column.getKey(), DbUtils.blobToBytes(res.getBlob(column
                            .getKey())));
                    } else if ("long".equals(column.getValue())) {
                        records.put(column.getKey(), res.getLong(column.getKey()));
                    }
                }
                results.add(records);
            }
        } catch (SQLException exp) {
            exp.printStackTrace();
        }
        return results;
    }

    /**
     * 检查表是否存在.
     */
    public static boolean existTable(Map map, String tableName) throws SQLException {
        String ip = map.get("ip").toString();
        String port = map.get("port").toString();
        String databaseName = map.get("dbname").toString();
        String user = map.get("username").toString();
        String password = map.get("password").toString();
        String connectStr = "jdbc:oracle:thin:@" + ip + ":" + port + ":" + databaseName;

        final String sql = "SELECT * FROM user_tables WHERE TABLE_NAME='%s'";

        DbUtils dbUtils = new DbUtils();
        ResultSet res = dbUtils.registerDriver(driverName).connection(connectStr, user, password)
            .executeQuery(sql);
        if (res.next() == false) {
            return false;
        } else {
            return true;
        }
    }
}
