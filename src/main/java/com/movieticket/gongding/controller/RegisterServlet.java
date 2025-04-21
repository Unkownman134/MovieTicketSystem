package com.movieticket.gongding.controller;

import com.movieticket.gongding.service.UserServiceFront;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private final UserServiceFront userServiceFront = new UserServiceFront();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String email = request.getParameter("email");
        BigDecimal money = new BigDecimal(request.getParameter("money").trim());

        try {
            int result = userServiceFront.register(username, password, email, money);

            if (result == 1) {
                response.getWriter().write("success");
            } else if (result == -1) {
                response.getWriter().write("exists");
            } else {
                response.getWriter().write("system_error");
            }
        } catch (Exception e) {
            response.getWriter().write("system_error");
        }
    }
}
