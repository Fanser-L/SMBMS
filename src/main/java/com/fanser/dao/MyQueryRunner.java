package com.fanser.dao;

import com.fanser.util.JDBCUtils;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
 * 适用于任何表的增删改查
 * 输入值为sql语句，占位符的内容
 * */
public class MyQueryRunner {
    public int update(String sql , Object... args) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        int row = 0;
        try {
            //1. 获取连接
            conn = JDBCUtils.getConnection();
            conn.setAutoCommit(false);//开启事务
            //2. 获取PreparedStatement,发送sql语句
            ps = conn.prepareStatement(sql);
            //3. 给占位符赋值
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i+1,args[i]);
            }
            //4. 执行sql语句
            row = ps.executeUpdate();
            //提交事务
            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
            //事务回滚
            conn.rollback();
        } finally {
            //5. 关闭连接
            JDBCUtils.close(conn,ps,null);
        }
        return row;
    }
    //缺点，只能用于已经确定的sql语句，也就是说不能在动态拼接的sql语句中使用
    public <T> List<T> queryList(String sql , Class<T> clazz ,Object ... args){
        List<T> list = null;
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            list = new ArrayList<>();
            //1.获取连接
            conn = JDBCUtils.getConnection();
            //2.获取PreparedStatement，发送sql语句
            ps = conn.prepareStatement(sql);
            //3.给占位符赋值
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i+1,args[i]);
            }
            //4.执行sql语句
            rs = ps.executeQuery();
            //5.获取列数
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            while(rs.next()){
                //6.封装进对象内
                T t = clazz.newInstance();
                for (int i = 0; i < columnCount; i++) {

                    //6.1 获取结果集的列名（别名）
                    String columnName = rsmd.getColumnLabel(i+1);
                    //6.2 根据列名（别名）来获取列值
                    Object columnValue = rs.getObject(columnName);

                    //10. 由于使用的类未知，所以需要利用反射来进行赋值
                    Field field = clazz.getDeclaredField(columnName);// 注意：结果集的列名（别名）必须和属性名保持一致
                    field.setAccessible(true);//忽略访问权限
                    field.set(t,columnValue);  //这里每次只装一个数据，但是在两层循环下能将全部数据都装进去
                }
                list.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JDBCUtils.close(conn,ps,rs);
        }

        return list;
    }

    public <T> T query(String sql ,Class<T> clazz, Object ... args){

        Connection conn = null;
        T t = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            //1. 获取连接
            conn = JDBCUtils.getConnection();
            //2. 获取PreparedStatement,发送sql语句
            ps = conn.prepareStatement(sql);
            //3. 给占位符赋值,注意mysql中索引是从1开始的，所以i+1
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i+1,args[i]);
            }
            //4. 执行sql语句，获取ResultSet结果集  获取到了有几行，指针是在行数据的前一行，所以需要next()
            rs = ps.executeQuery();
            //5. 获取结果集的元数据 ResultSetMetaData()  为了下一步获取有几列
            ResultSetMetaData rsmd = rs.getMetaData();
            //6. 获取结果集的列数
            int columnCount = rsmd.getColumnCount();
            //7. 已经有列数了，所以对每一列进行获取数据
            if (rs.next()){  //看似是个if条件选择，实际上是个循环语句
                //需要将这些散列的数据封装进对象里头，更方便管理，这里是只封装进单个对象,
                t = clazz.newInstance();
                for (int i = 0; i < columnCount; i++) {
                    //8. 获取结果集列名，列别名
                    String columnName = rsmd.getColumnLabel(i + 1);

                    //9. 获取结果集中对应列值，由于不知道列值是什么类型的  所以这里使用Object代替
                    Object columnValue = rs.getObject(columnName);

                    //10. 由于使用的类未知，所以需要利用反射来进行赋值
                    Field field = clazz.getDeclaredField(columnName);// 注意：结果集的列名（别名）必须和属性名保持一致
                    field.setAccessible(true);//忽略访问权限
                    field.set(t,columnValue);  //这里每次只装一个数据，但是在两层循环下能将全部数据都装进去
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //5. 关闭连接
            JDBCUtils.close(conn,ps,rs);
        }
        return t;
    }

}
