package com.fanser.util;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class JDBCUtils {
    /*
    * 获取连接，关闭连接
    * @return
    * @Exception
    * */
    public static Connection getConnection() throws Exception {

        Properties props = new Properties();
        //属性文件路径，如 user password driverClassName 等等
        InputStream insss =JDBCUtils.class.getResourceAsStream("/db.properties");
        props.load(insss);
//        props.load(JDBCUtils.class.getClass().getClassLoader().getResourceAsStream("src/jdbc.properties"));
        String driverClassName = props.getProperty("driver");
        String url = props.getProperty("url");
        String user = props.getProperty("username");
        String password = props.getProperty("password");

//        1.加载驱动
        Class clazz = Class.forName(driverClassName);
        // 2.获取连接
        Connection conn = DriverManager.getConnection(url,user,password);

        return conn;

    }
    //关闭连接，自下而上
    public  static void close(Connection conn , PreparedStatement ps, ResultSet rs){

        if (rs != null){
            try {
                rs.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        if (ps !=null){
            try {
                ps.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        if (conn != null){
            try {
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

    }

}
