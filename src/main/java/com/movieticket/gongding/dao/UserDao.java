package com.movieticket.gongding.dao;

import com.movieticket.gongding.entity.User;
import com.movieticket.gongding.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserDao {
    public boolean addUser(User user) {
        String sql = "INSERT INTO users (username, password_hash, salt, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getSalt());
            pstmt.setString(4, user.getEmail());

            //影响行数大于0为注册成功返回true
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
