package com.fanser.dao.user;

import com.fanser.dao.BaseDao;
import com.fanser.dao.MyQueryRunner;
import com.fanser.entity.User;
import com.mysql.cj.util.StringUtils;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDaoImpl implements UserDao {

    @Override
    public User getLoginUser(String userCode, String userPassword) {
        User user = null;

        String sql = "select * from smbms_user where userCode=?";
        MyQueryRunner queryRunner = new MyQueryRunner();
//        Object[] params = {userCode};
        user = queryRunner.query(sql, User.class, userCode);
        return user;
    }

    @Override
    public int updatePassword(String newPassword, long id) throws SQLException {
        String sql = "UPDATE `smbms_user` SET `userPassword`=? WHERE `id`=? ";

        MyQueryRunner queryRunner = new MyQueryRunner();
        int update = queryRunner.update(sql, newPassword, id);
        return update;
    }

    //根据用户名或角色查询用户总数
    //就很烦，没办法把自己的类用上，这个可变的拼接sql会产生可变的参数，我的查询表操作需要的是固定参数的传入
    @Override
    public int getUserCount(Connection connection, String userName, int userRole) throws Exception {
        int count = 0;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        if (connection != null) {
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT COUNT(*) AS count FROM `smbms_user` u,`smbms_role` r WHERE u.`userRole`=r.`id`");
            ArrayList<Object> list = new ArrayList<Object>();//存放可能会放进sql里的参数，就是用来替代?的params
            if (!StringUtils.isNullOrEmpty(userName)) {
                sql.append(" and u.username like ?");
                list.add("%" + userName + "%");//模糊查询，index:0
            }
            if (userRole > 0) {
                sql.append(" and u.userRole = ?");
                list.add(userRole);//index:1
            }
            Object[] params = list.toArray();//转换成数组
            System.out.println("当前的sql语句为------------>" + sql);
            rs = BaseDao.execute(connection, sql.toString(), params, rs, pstm);
            if (rs.next()) {
                count = rs.getInt("count");

            }
            BaseDao.closeResource(null, pstm, rs);

        }
        return count;
    }

    //获取用户列表通过用户输入的条件
    @Override
    //通过用户输入的条件查询用户列表
    public List<User> getUserList(Connection connection, String userName, int userRole, int currentPageNo, int pageSize) throws Exception {
        List<User> userList = new ArrayList<User>();
        PreparedStatement pstm = null;
        ResultSet rs = null;
        if (connection != null) {
            StringBuffer sql = new StringBuffer();
            sql.append("select u.*,r.roleName as userRoleName from smbms_user u,smbms_role r where u.userRole = r.id");
            List<Object> list = new ArrayList<Object>();
            if (!StringUtils.isNullOrEmpty(userName)) {
                sql.append(" and u.userName like ?");
                list.add("%" + userName + "%");
            }
            if (userRole > 0) {
                sql.append(" and u.userRole = ?");
                list.add(userRole);
            }
            sql.append(" order by creationDate DESC limit ?,?");
            currentPageNo = (currentPageNo - 1) * pageSize;
            list.add(currentPageNo);
            list.add(pageSize);

            Object[] params = list.toArray();
            System.out.println("sql ----> " + sql.toString());
            rs = BaseDao.execute(connection, sql.toString(), params, rs, pstm);
            while (rs.next()) {
                User _user = new User();
                _user.setId((long) rs.getInt("id"));
                _user.setUserCode(rs.getString("userCode"));
                _user.setUserName(rs.getString("userName"));
                _user.setGender(rs.getInt("gender"));
                _user.setBirthday(rs.getDate("birthday"));
                _user.setPhone(rs.getString("phone"));
                _user.setUserRole(rs.getInt("userRole"));
                _user.setUserRoleName(rs.getString("userRoleName"));
                userList.add(_user);
            }
            BaseDao.closeResource(null, pstm, rs);
        }
        return userList;
    }

    //好像需要的参数不多，我又可以简化点使用自己的工具类了
    @Override
    public int addUser(Connection connection, User user) throws SQLException {
        PreparedStatement pstm = null;
        int updateNum = 0;
        if (connection != null) {
            //这sql属实有点长了，属性有点多
            String sql = "insert into smbms_user ( userCode, userName," +
                    " userPassword,userRole, gender, birthday, phone, address" +
                    " )" +
                    "VALUES(?,?,?,?,?,?,?,?) ;";
            Object[] params = {user.getUserCode(), user.getUserName(), user.getUserPassword(),
                    user.getUserRole(), user.getGender(), user.getBirthday(),
                    user.getPhone(), user.getAddress()};
//            updateNum = BaseDao.execute(connection, sql, params, pstm);
            MyQueryRunner myQueryRunner = new MyQueryRunner();
            updateNum = myQueryRunner.update(sql, params);
//            BaseDao.closeResource(null, pstm, null);
        }
        return updateNum;
    }

    @Override
    public int deleteUser(long userid) throws SQLException {
        String sql = "delete from smbms_user where id = ?";
        MyQueryRunner myQueryRunner = new MyQueryRunner();
        int update = myQueryRunner.update(sql, userid);
        return update;
    }

    @Override
    public User getUserById(String id) throws Exception {
        String sql = "select u.*,r.roleName as userRoleName from smbms_user u,smbms_role r where u.id=? and u.userRole = r.id";
        Object[] params={id};
        MyQueryRunner myQueryRunner = new MyQueryRunner();
        User user = myQueryRunner.query(sql, User.class, params);
        System.out.println(user);
        return user;
    }

    @Override
    public int modify(User user) throws Exception {
        int updateNum = 0;
        String sql = "update smbms_user set userName=?,"+
                "gender=?,birthday=?,phone=?,address=?,userRole=?,modifyBy=?,modifyDate=? where id = ? ";
        Object[] params = {user.getUserName(),user.getGender(),user.getBirthday(),
                user.getPhone(),user.getAddress(),user.getUserRole(),user.getModifyBy(),
                user.getModifyDate(),user.getId()};
        MyQueryRunner myQueryRunner = new MyQueryRunner();
        updateNum = myQueryRunner.update(sql,params);
        return updateNum;
    }
}

