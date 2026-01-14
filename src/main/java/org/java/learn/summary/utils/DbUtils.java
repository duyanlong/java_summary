package org.java.learn.summary.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class DbUtils {

    private Connection con = null;
    private PreparedStatement pstmt = null;
    private ResultSet rs = null;


    /**
     * 注册驱动.
     */
    public DbUtils registerDriver(String driver) {
        try {
            Class.forName(driver).newInstance();
            // LOGGER.info("注册驱动成功: {}", driver);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        } catch (InstantiationException exp) {
            exp.printStackTrace();
        } catch (IllegalAccessException exp) {
            exp.printStackTrace();
        }
        return this;
    }

    /**
     * 初始化连接.
     */
    public DbUtils connection(String conStr, String user, String password) {
        try {
            Properties param = new Properties();
            param.setProperty("user", user);
            param.setProperty("password", password);
            // LOGGER.info("连接数据库: {},{},{}", conStr, user, password);
            con = DriverManager.getDriver(conStr).connect(conStr, param);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return this;
    }

    /**
     * 初始化连接.
     */
    public Connection getConnection(String conStr, String user, String password) {
        Connection connection = null;
        try {
            Properties properties = new Properties();
            properties.put("user", user);
            properties.put("password", password);
            // LOGGER.info("连接数据库: {},{},{}", conStr, user, password);
            connection = DriverManager.getDriver(conStr).connect(conStr, properties);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return connection;
    }

    /**
     * 手工控制事务.
     */
    public DbUtils connectionNoAutoTran(String conStr, String user, String password) {
        try {
            con = DriverManager.getConnection(conStr, user, password);
            con.setAutoCommit(false);
            // LOGGER.info("连接数据库成功: {},{},{}", conStr, user, password);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return this;
    }

    /**
     * 执行修改操作.
     */
    public Integer executeUpdate(String sql) throws SQLException {
        pstmt = con.prepareStatement(sql);
        return pstmt.executeUpdate();
    }

    /**
     * 手工控制事务.
     */
    public Integer executeUpdateNoAutoTran(String sql) throws SQLException {
        pstmt = con.prepareStatement(sql);
        int flag = pstmt.executeUpdate();
        con.commit();
        return flag;
    }

    /**
     * 执行查询操作.
     */
    public ResultSet executeQuery(String sql) throws SQLException {
        pstmt = con.prepareStatement(sql);
        return pstmt.executeQuery();
    }

    /**
     * 关闭数据库连接.
     */
    public void closeDb() {
        try {
            if (rs != null) {
                rs.close();
            }
            if (pstmt != null) {
                pstmt.close();
            }
            if (con != null) {
                con.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            rs = null;
            pstmt = null;
            con = null;
        }
    }

    /**
     * 关闭数据库连接.
     */
    public void closeDb(ResultSet rs, PreparedStatement pstmt, Connection con) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (pstmt != null) {
                pstmt.close();
            }
            if (con != null) {
                con.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            rs = null;
            pstmt = null;
            con = null;
        }
    }

    /**
     * 将blob转化为byte[],可以转化二进制流的
     *
     * @param blob
     * @return
     */
    public static byte[] blobToBytes(Blob blob) {
        if(blob==null){
            return new byte[]{};
        }
        InputStream is = null;
        byte[] b = null;
        try {
            is = blob.getBinaryStream();
            b = new byte[(int) blob.length()];
            is.read(b);
            return b;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
                is = null;
            } catch (IOException e) {
                e.getCause();
            }
        }
        return b;
    }

}
