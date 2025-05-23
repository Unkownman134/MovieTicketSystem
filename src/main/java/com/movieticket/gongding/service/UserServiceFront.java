package com.movieticket.gongding.service;

import com.movieticket.gongding.dao.UserDao;

import com.movieticket.gongding.entity.User;
import com.movieticket.gongding.utils.PasswordUtils;

import java.math.BigDecimal;

public class UserServiceFront {
    private final UserDao userDao = new UserDao();

    //验证用户登录
    public int authenticate(String username, String password) {
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

    public int register(String username, String passwordHash, String email, BigDecimal money) {
        try {
            //注册用户名已存在
            if (userDao.findUserByUsername(username) != null) {
                return -1;
            }

            String salt = PasswordUtils.generateSalt();
            String hashedPassword = PasswordUtils.hashPassword(passwordHash, salt);

            User user = new User();
            user.setUsername(username);
            user.setPasswordHash(hashedPassword);
            user.setEmail(email);
            user.setSalt(salt);
            user.setMoney(money);

            return userDao.addUser(user) == true ? 1 : -2;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
