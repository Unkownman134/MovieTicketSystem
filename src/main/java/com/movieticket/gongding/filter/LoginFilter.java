package com.movieticket.gongding.filter;

import com.movieticket.gongding.dao.UserDao;
import com.movieticket.gongding.entity.User;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebFilter("/*")
public class LoginFilter implements Filter {

    private static final String[] EXCLUDED_PATHS = {"/register", "/login", "/html/login.html", "/css/", "/js/", "/images/", "/html/register.html"};

    private final UserDao userDao = new UserDao();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();

        boolean isExcluded = false;
        for (String path : EXCLUDED_PATHS) {
            if (uri.startsWith(contextPath + path)) {
                isExcluded = true;
                break;
            }
        }

        if (isExcluded) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        boolean loggedIn = (session != null && session.getAttribute("user") != null);

        if (!loggedIn) {
            response.sendRedirect(contextPath + "/html/login.html");
            return;
        }

        String requestUri = request.getRequestURI();
        if (requestUri.contains("/html/user.html")) {
            User sessionUser = (User) session.getAttribute("user");
            String paramUsername = request.getParameter("username");

            if (paramUsername == null || !paramUsername.equals(sessionUser.getUsername())) {
                response.sendRedirect(contextPath + "/html/login.html");
                return;
            }

            User realUser = userDao.findUserByUsername(paramUsername);
            if (realUser == null) {
                session.invalidate();
                response.sendRedirect(contextPath + "/html/login.html");
                return;
            }
        }

        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}
}