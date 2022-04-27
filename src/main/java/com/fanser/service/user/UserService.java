package com.fanser.service.user;

import com.fanser.entity.User;

import java.sql.SQLException;
import java.util.List;

public interface UserService {
    //登录操作
    public User login(String UserCode,String UserPassword);
    //修改密码操作
    public int update(String newPassword,long id) throws SQLException;
    //根据条件（用户的查询输入）查询用户记录数
    public int getUserCount(String queryUserName, int queryUserRole) throws SQLException;
    //根据条件查询用户列表
    public List<User> getUserList(String queryUserName, int queryUserRole, int currentPageNo, int pageSize) throws Exception;
    //用户管理模块中的 子模块—— 添加用户
    public Boolean add(User user) throws SQLException;
    //用户管理模块中的 子模块—— 删除用户
    public int delete(long userid) throws SQLException;
    //通过id查询用户
    public User findUserById(String id) throws Exception;
    //修改用户信息
    public Boolean modify(User user) throws Exception;
}
