package com.movieticket.gongding.service;

import com.movieticket.gongding.dao.UserDao;
import com.movieticket.gongding.entity.User;
import com.movieticket.gongding.utils.PasswordUtils;

public class UserService {
    private final UserDao userDao = new UserDao();

    //用户注册服务
    public boolean register(String username, String passwordHash, String email) {
        try {
            //注册用户名已存在
            if (userDao.findUserByUsername(username) != null) {
                return false;
            }

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

    public int login(String username, String password) {
        try {
            User user = userDao.findUserByUsername(username);
            if (user == null) {
                //用户不存在
                return -1;
            }
            if ("BLACKLIST".equals(user.getStatus())) {
                //用户被拉黑
                return -2;
            }

            String calculatedHash = PasswordUtils.hashPassword(password, user.getSalt());
            if (!calculatedHash.equals(user.getPasswordHash())) {
                //密码错误
                return -3;
            }

            //更新用户登录时间
            userDao.updateLoginTime(user.getId());
            //登录成功
            return 1;
        } catch (Exception e) {
            //系统错误
            return -4;
        }
    }
}
