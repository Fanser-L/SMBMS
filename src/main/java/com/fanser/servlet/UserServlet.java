package com.fanser.servlet;

import com.alibaba.fastjson.JSONArray;
import com.fanser.entity.Role;
import com.fanser.entity.User;
import com.fanser.service.role.RoleService;
import com.fanser.service.role.RoleServiceImpl;
import com.fanser.service.user.UserService;
import com.fanser.service.user.UserServiceImpl;
import com.fanser.util.Constants;
import com.fanser.util.PageSupport;
import com.mysql.cj.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getParameter("method");
        try {
            if (method.equals("savepwd")) {
                this.savePwd(req, resp);
            } else if (method.equals("pwdmodify")) {
                this.modifyPwd(req, resp);
            } else if (method.equals("query")) {
                this.query(req, resp);
            } else if (method != null && method.equals("add")) {
                this.add(req, resp);
            } else if (method != null && method.equals("ucexist")) {
                this.ifExist(req, resp);
            } else if (method != null && method.equals("getrolelist")) {
                //查询用户角色表
                this.getRoleList(req, resp);
            } else if (method != null && method.equals("deluser")) {
                this.deluser(req, resp);
            } else if (method != null && method.equals("view")) {
                this.getUserById(req, resp, "userview.jsp");
            } else if (method != null && method.equals("modify")) {
                this.getUserById(req, resp, "usermodify.jsp");
            }else if(method != null && method.equals("modifyexe")){
                //验证用户
                this.modify(req, resp);
            }else if(method != null && method.equals("pwdmodify")){
                //验证用户密码
                this.modifyPwd(req, resp);
            }else if(method != null && method.equals("savepwd")){
                //更新用户密码
                this.savePwd(req, resp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    //用户修改密码方法
    public void savePwd(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, SQLException {
        //从Session中获取ID
        Object obj = req.getSession().getAttribute(Constants.USER_SESSION);
        //获取前端页面传来的旧密码
        String oldpassword = req.getParameter("oldpassword");
        //获取前端页面传来的新密码
        String newpassword = req.getParameter("newpassword");
        System.out.println(oldpassword);
        System.out.println(newpassword);
        //先判断不为空 再比较密码是否相等
//
        if (obj != null && newpassword != null && oldpassword != null) {
            User user = (User) obj;
            //如果用户本身的密码 与 前端传来的旧密码 不同
            if (!user.getUserPassword().equals(oldpassword)) {
                req.getSession().setAttribute("message", "旧密码填写错误!");
                //如果旧密码输入正确
            } else {
                UserService userService = new UserServiceImpl();
//                //修改密码并返回结果
                int flag = userService.update(newpassword, user.getId());
//                //如果密码修改成功 移除当前session
                if (flag > 0) {
                    req.setAttribute("message", "修改密码成功，请使用新密码登录!");
                    req.getSession().removeAttribute(Constants.USER_SESSION);
                } else {
                    req.setAttribute("message", "密码修改失败 新密码不符合规范");
                }
            }

        } else {
            req.setAttribute("message", "新密码不能为空!");
        }
        //修改完了 重定向到此修改页面
        req.getRequestDispatcher("pwdmodify.jsp").forward(req, resp);
    }

    //验证密码的方法
    public void modifyPwd(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //依旧从session中取ID
        Object obj = req.getSession().getAttribute(Constants.USER_SESSION);
        //取前端传来的旧密码
        String oldpassword = req.getParameter("oldpassword");
        //将结果存放在map集合中 让Ajax使用
        Map<String, String> resultMap = new HashMap<>();
        //下面开始判断 键都是用result 此处匹配js中的Ajax代码
        if (obj == null) {
            //说明session被移除了 或未登录|已注销
            resultMap.put("result", "sessionerror");
        } else if (oldpassword == null) {
            //前端输入的密码为空
            resultMap.put("result", "error");
        } else {
            //如果旧密码与前端传来的密码相同
            if (((User) obj).getUserPassword().equals(oldpassword)) {
                resultMap.put("result", "true");
            } else {
                //前端输入的密码和真实密码不相同
                resultMap.put("result", "false");
            }
        }
        //上面已经封装好 现在需要传给Ajax 格式为json 所以我们得转换格式
        resp.setContentType("application/json");//将应用的类型变成json
        PrintWriter writer = resp.getWriter();
        //JSONArray 阿里巴巴的JSON工具类 用途就是：转换格式
        writer.write(JSONArray.toJSONString(resultMap));
        writer.flush();
        writer.close();
    }

    //查询用户列表的方法
    private void query(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, SQLException {
        //接收前端传来的参数
        String queryUserName = req.getParameter("queryname");
        String temp = req.getParameter("queryUserRole");//从前端传回来的用户角色码不知是否为空或者是有效角色码，所以暂存起来
        String pageIndex = req.getParameter("pageIndex");
        int queryUserRole = 0;
        //先设置一个默认的用户角色码，若temp为空，则将这个传进sql语句中，这是真正放进sql语句中的角色码
        /*
            if(userRole > 0){
                sql.append(" and u.userRole = ?");
                list.add(userRole);
            }
            这句便不会被执行
         */
        //通过UserServiceImpl得到用户列表,用户数
        UserServiceImpl userService = new UserServiceImpl();
        //通过RoleServiceImpl得到角色表
        RoleService roleService = new RoleServiceImpl();
        List<User> userList = null;//用来存储用户列表
        List<Role> roleList = null;//用来存储角色表
        //设置每页显示的页面容量
        int pageSize = Constants.pageSize;
        //设置当前的默认页码
        int currentPageNo = 1;
        //输出控制台，显示参数的当前值
        System.out.println("queryUserName servlet--------" + queryUserName);
        System.out.println("queryUserRole servlet--------" + queryUserRole);
        System.out.println("query pageIndex--------- > " + pageIndex);

        //前端传来的参数若不符合查询sql语句，即如果用户不进行设置，值为空会影响sql查询，需要给它们进行一些约束
        if (queryUserName == null) {//这里为空，说明用户没有输入要查询的用户名，则sql语句传值为""，%%，会查询所有记录
            queryUserName = "";
        }
        if (temp != null && !temp.equals("")) {
            //不为空，说明前端有传来的用户所设置的userCode，更新真正的角色码
            queryUserRole = Integer.parseInt(temp);//强制转换，前端传递的参数都是默认字符串，要转成int类型
        }

        if (pageIndex != null) {//说明当前用户有进行设置跳转页面
            currentPageNo = Integer.valueOf(pageIndex);
        }

        //有了用户名和用户角色后可以开始查询了，所以需要显示当前查询到的总记录条数
        int totalCount = userService.getUserCount(queryUserName, queryUserRole);
        //根据总记录条数以及当前每页的页面容量可以算出，一共有几页，以及最后一页的显示条数
        PageSupport pageSupport = new PageSupport();
        pageSupport.setCurrentPageNo(currentPageNo);
        pageSupport.setPageSize(pageSize);
        pageSupport.setTotalCount(totalCount);
        //可显示的总页数
        int totalPageCount = pageSupport.getTotalPageCount();

        //约束首位页，即防止用户输入的页面索引小于1或者大于总页数
        if (currentPageNo < 1) {
            currentPageNo = 1;
        } else if (currentPageNo > totalPageCount) {
            currentPageNo = totalPageCount;
        }
        //有了，待查询条件，当前页码，以及每页的页面容量后，就可以给出每页的具体显示情况了
        userList = userService.getUserList(queryUserName, queryUserRole, currentPageNo, pageSize);
        roleList = roleService.getRoleList();
        //得到了用户表与角色表以及各种经过处理后的参数，都存进req中
        req.setAttribute("userList", userList);
        req.setAttribute("roleList", roleList);
        req.setAttribute("queryUserName", queryUserName);
        req.setAttribute("queryUserRole", queryUserRole);
        req.setAttribute("totalPageCount", totalPageCount);
        req.setAttribute("totalCount", totalCount);
        req.setAttribute("currentPageNo", currentPageNo);
        //将所得到的的所有req参数送回给前端
        req.getRequestDispatcher("userlist.jsp").forward(req, resp);
    }

    //Servlet类中添加判断用户编码是否存在的方法
    private void ifExist(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        //获取前端输入的用户编码
        String userCode = req.getParameter("userCode");
        UserService userService = new UserServiceImpl();
        User isNullUser = userService.login(userCode, "");
        //判断是否已经存在这个用户编码
        boolean flag = isNullUser != null ? true : false;
        //将结果存放在map集合中 让Ajax使用
        Map<String, String> resultMap = new HashMap<>();
        if (flag) {
            //用户编码存在
            //将信息存入map中
            resultMap.put("userCode", "exist");
        }
        //上面已经封装好 现在需要传给Ajax 格式为json 所以我们得转换格式
        resp.setContentType("application/json");//将应用的类型变成json
        PrintWriter writer = resp.getWriter();
        //JSONArray 阿里巴巴的JSON工具类 用途就是：转换格式
        writer.write(JSONArray.toJSONString(resultMap));
        writer.flush();
        writer.close();
    }

    //获取用户角色列表，添加用户时作为选项选择角色
    private void getRoleList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, SQLException {
        List<Role> roleList = null;
        RoleService roleService = new RoleServiceImpl();
        roleList = roleService.getRoleList();
        //把roleList转换成json对象输出
        resp.setContentType("application/json");
        PrintWriter outPrintWriter = resp.getWriter();
        outPrintWriter.write(JSONArray.toJSONString(roleList));
        outPrintWriter.flush();
        outPrintWriter.close();
    }

    //添加用户
    private void add(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException, SQLException {
        System.out.println("当前正在执行增加用户操作");
        //从前端得到页面的请求的参数即用户输入的值
        String userCode = req.getParameter("userCode");
        String userName = req.getParameter("userName");
        String userPassword = req.getParameter("userPassword");
        String userRole = req.getParameter("userRole");
        //String ruserPassword = req.getParameter("ruserPassword");
        String gender = req.getParameter("gender");
        String birthday = req.getParameter("birthday");
        String phone = req.getParameter("phone");
        String address = req.getParameter("address");
        //把这些值塞进一个用户属性中
        User user = new User();
        user.setUserCode(userCode);
        user.setUserName(userName);
        user.setUserPassword(userPassword);
        user.setAddress(address);
        user.setGender(Integer.valueOf(gender));
        user.setPhone(phone);
        try {
            user.setBirthday(new SimpleDateFormat("yyyy-MM-dd").parse(birthday));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        user.setUserRole(Integer.valueOf(userRole));
        user.setCreationDate(new Date());
        //查找当前正在登陆的用户的id
        user.setCreatedBy(((User) req.getSession().getAttribute(Constants.USER_SESSION)).getId());
        UserServiceImpl userService = new UserServiceImpl();
        boolean flag = userService.add(user);
        //如果添加成功，则页面转发，否则重新刷新，再次跳转到当前页面
        if (flag) {
            resp.sendRedirect(req.getContextPath() + "/jsp/user.do?method=query");
        } else {
            req.getRequestDispatcher("useradd.jsp").forward(req, resp);
        }
    }

    private void deluser(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        String id = req.getParameter("uid");
        Integer delId = 0;
        try {
            delId = Integer.parseInt(id);
        } catch (Exception e) {
            // TODO: handle exception
            delId = 0;
        }
        //需要判断是否能删除成功
        HashMap<String, String> resultMap = new HashMap<String, String>();
        if (delId <= 0) {
            resultMap.put("delResult", "notexist");
        } else {
            UserService userService = new UserServiceImpl();
            if (userService.delete(delId) > 0) {
                resultMap.put("delResult", "true");
            } else {
                resultMap.put("delResult", "false");
            }
        }

        //把resultMap转换成json对象输出
        resp.setContentType("application/json");
        PrintWriter outPrintWriter = resp.getWriter();
        outPrintWriter.write(JSONArray.toJSONString(resultMap));
        outPrintWriter.flush();
        outPrintWriter.close();
    }

    private void getUserById(HttpServletRequest req, HttpServletResponse resp, String url) throws Exception {
        String id = req.getParameter("uid");
        if (!StringUtils.isNullOrEmpty(id)) {//判断前端传入的属性不为空
            //调用后台方法得到user对象
            UserService userService = new UserServiceImpl();
            User user = userService.findUserById(id);
            req.setAttribute("user", user);
            req.getRequestDispatcher(url).forward(req, resp);
        }
    }

    //修改用户信息
    private void modify(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //需要拿到前端传递进来的参数
        String id = req.getParameter("uid");
        ;
        String userName = req.getParameter("userName");
        String gender = req.getParameter("gender");
        String birthday = req.getParameter("birthday");
        String phone = req.getParameter("phone");
        String address = req.getParameter("address");
        String userRole = req.getParameter("userRole");

        //创建一个user对象接收这些参数
        User user = new User();
        user.setId(Long.valueOf(id));
        user.setUserName(userName);
        user.setGender(Integer.valueOf(gender));
        try {
            user.setBirthday(new SimpleDateFormat("yyyy-MM-dd").parse(birthday));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        user.setPhone(phone);
        user.setAddress(address);
        user.setUserRole(Integer.valueOf(userRole));
        user.setModifyBy(((User) req.getSession().getAttribute(Constants.USER_SESSION)).getId());
        user.setModifyDate(new Date());

        //调用service层
        UserServiceImpl userService = new UserServiceImpl();
        Boolean flag = userService.modify(user);

        //判断是否修改成功来决定跳转到哪个页面
        if (flag) {
            resp.sendRedirect(req.getContextPath() + "/jsp/user.do?method=query");
        } else {
            req.getRequestDispatcher("usermodify.jsp").forward(req, resp);
        }

    }
}
