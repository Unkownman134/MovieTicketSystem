package com.movieticket.gongding.dao;

import com.movieticket.gongding.entity.User;
import com.movieticket.gongding.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

    public User findUserByUsername(String username) throws SQLException {
        try (Connection conn = JDBCUtils.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setUsername(rs.getString("username"));
                        user.setPasswordHash(rs.getString("password_hash"));
                        user.setSalt(rs.getString("salt"));
                        user.setEmail(rs.getString("email"));
                        user.setStatus(rs.getString("status"));
                        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                        user.setLastLogin(rs.getTimestamp("last_login") != null ?
                                rs.getTimestamp("last_login").toLocalDateTime() : null);
                        return user;
                    }
                    return null;
                }
            }
        }
    }

    public boolean updateLoginTime(int userId) {
        String sql = "UPDATE users SET last_login = NOW() WHERE id = ?";
        try (Connection conn = JDBCUtils.getConnection();PreparedStatement pstmt =conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
