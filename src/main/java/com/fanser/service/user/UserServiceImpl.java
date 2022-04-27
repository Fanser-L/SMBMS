package com.fanser.service.user;

import com.fanser.dao.BaseDao;
import com.fanser.dao.user.UserDao;
import com.fanser.dao.user.UserDaoImpl;
import com.fanser.entity.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class UserServiceImpl implements UserService{
    //业务层都会调用dao层，所以我们要引入Dao层；
    private UserDao userDao;
    public UserServiceImpl(){
        //无参构造中调用UserDao，确保创建类的时候必定会创建对应的类
        userDao = new UserDaoImpl();
    }
    //实现登录操作的业务
    @Override
    public User login(String userCode, String userPassword) {
        User user = userDao.getLoginUser(userCode,userPassword);
        return user;
    }
    //实现更新操作的业务
    @Override
    public int update(String newPassword,long id) throws SQLException {
        int i = userDao.updatePassword(newPassword,id);
        return i;
    }

    @Override
    public int getUserCount(String queryUserName, int queryUserRole) {
        int count=0;
        Connection connection=null;
        try {
            connection = BaseDao.getConnection();
            count = userDao.getUserCount(connection, queryUserName, queryUserRole);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            BaseDao.closeResource(connection, null, null);
        }
        return count;
    }

    @Override
    public List<User> getUserList(String queryUserName, int queryUserRole, int currentPageNo, int pageSize) {
        // TODO Auto-generated method stub
        Connection connection = null;
        List<User> userList = null;
        System.out.println("queryUserName ---- > " + queryUserName);
        System.out.println("queryUserRole ---- > " + queryUserRole);
        System.out.println("currentPageNo ---- > " + currentPageNo);
        System.out.println("pageSize ---- > " + pageSize);
        try {
            connection = BaseDao.getConnection();
            userList = userDao.getUserList(connection, queryUserName,queryUserRole,currentPageNo,pageSize);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }finally{
            BaseDao.closeResource(connection, null, null);
        }
        return userList;
    }

    @Override
    public Boolean add(User user) {
        boolean flag = false;
        Connection connection = null;
        try {
            connection = BaseDao.getConnection();//获得连接
            connection.setAutoCommit(false);//开启JDBC事务管理
            int updateRows = userDao.addUser(connection,user);
            connection.commit();
            if(updateRows > 0){
                flag = true;
                System.out.println("add success!");
            }else{
                System.out.println("add failed!");
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            try {
                System.out.println("rollback==================");
                connection.rollback();//失败就回滚
            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }finally{
            //在service层进行connection连接的关闭
            BaseDao.closeResource(connection, null, null);
        }
        return flag;
    }

    @Override
    public int delete(long userid) throws SQLException {
        int i = userDao.deleteUser(userid);
        return i;
    }

    @Override
    public User findUserById(String id) throws Exception {
        User user = userDao.getUserById(id);
        return user;
    }

    @Override
    public Boolean modify(User user) throws Exception {
        boolean flag = false;
        int updateNum = userDao.modify(user);
        if (updateNum>0){
            flag = true;
        }else flag = false;
        return flag;
    }
    //    @Test
//    public void test() throws SQLException {
//        UserServiceImpl userService = new UserServiceImpl();
//        User login = userService.login("admin", "111");//这里只是完成了按用户名查询的操作，没有验证
//        int userCount = userService.getUserCount(null, 2);
//        System.out.println(userCount);
//    }

}
