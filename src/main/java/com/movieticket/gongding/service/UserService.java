package com.movieticket.gongding.service;

import com.movieticket.gongding.dao.UserDao;
import com.movieticket.gongding.entity.User;
import com.movieticket.gongding.utils.PasswordUtils;

public class UserService {
    private final UserDao userDao = new UserDao();

    //用户注册服务
    public boolean register(String username, String passwordHash, String email) {
        try {
            String salt = PasswordUtils.generateSalt();
            String hashedPassword = PasswordUtils.hashPassword(passwordHash, salt);

            User user = new User();
            user.setUsername(username);
            user.setPasswordHash(hashedPassword);
            user.setEmail(email);
            user.setSalt(salt);

            return userDao.addUser(user);
        } catch (Exception e) {
            return false;
        }
    }
}
