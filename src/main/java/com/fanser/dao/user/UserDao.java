package com.fanser.dao.user;

import com.fanser.entity.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface UserDao {
    //获取登陆用户
    public User getLoginUser(String userCode,String userPassword);
    //修改密码
    public int updatePassword(String newPassword,long id)throws SQLException;
    //根据用户输入的名字或者角色id来查询计算用户数量
    public int getUserCount(Connection connection, String userName, int userRole)throws Exception;
    //通过用户输入的条件查询用户列表
    public List<User> getUserList(Connection connection,String userName,int userRole,int currentPageNo,int pageSize) throws  Exception;
    //用户管理模块中的 —— 添加用户
    public abstract int addUser(Connection connection,User user)throws SQLException;
    //用户管理模块中的 —— 删除用户
    public abstract int deleteUser(long userid)throws SQLException;
    //通过userId查看当前用户信息
    public User getUserById(String id)throws Exception;
    //修改用户信息
    public int modify(User user)throws Exception;
}
