package com.fanser.servlet;

import com.fanser.entity.User;
import com.fanser.service.user.UserService;
import com.fanser.service.user.UserServiceImpl;
import com.fanser.util.Constants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginServlet extends HttpServlet {
    // Servlet 控制层 ，调用业务代码

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //获取view层中输入的用户名和密码
        String userCode = req.getParameter("userCode");
        String userPassword = req.getParameter("userPassword");
        //和数据库中的用户名密码进行比对
        UserService userService = new UserServiceImpl();
        User user = userService.login(userCode, userPassword);
        System.out.println(userCode);
        System.out.println(userPassword);
        if (user!=null) {//查有此人
            if (user.getUserPassword().equals(userPassword)){
                //将用户信息放进session
                req.getSession().setAttribute(Constants.USER_SESSION,user);
                //跳转到主页重定向
                resp.sendRedirect(req.getContextPath()+"/jsp/frame.jsp");
            }else{//查无此人，回归原页面，提示错误
                req.setAttribute("error","用户或者密码不正确，请重新登陆");
                req.getSession().removeAttribute(Constants.USER_SESSION);
                req.getRequestDispatcher("login.jsp").forward(req,resp);
            }
        }else{//查无此人，回归原页面，提示错误
            req.setAttribute("error","用户或者密码不正确，请重新登陆");
            req.getRequestDispatcher("login.jsp").forward(req,resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
