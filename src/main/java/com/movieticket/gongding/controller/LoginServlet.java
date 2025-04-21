package com.movieticket.gongding.controller;

import com.movieticket.gongding.dao.UserDao;
import com.movieticket.gongding.entity.User;
import com.movieticket.gongding.service.UserServiceFront;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final UserServiceFront userServiceFront = new UserServiceFront();
    private final UserDao userDao = new UserDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        int isValid = userServiceFront.authenticate(username, password);

        if (isValid == 1) {
            User user = userDao.findUserByUsername(username);
            if (user == null) {
                response.getWriter().write("system_error");
                return;
            }
            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getId());

            response.getWriter().write("success");
        } else {
            String result;
            switch (isValid) {
                case -1 -> result = "no_user";
                case -2 -> result = "blocked";
                case -3 -> result = "wrong_pass";
                default -> result = "system_error";
            }
            response.getWriter().write(result);
        }
    }
}
