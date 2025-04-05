package com.movieticket.gongding.utils;

import java.sql.*;

public class JDBCUtils {
    //finial声明保证安全性
    private static final String URL = "jdbc:mysql://localhost:3306/movie_ticket_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASSWORD = "T.GKiPl({xfLt0M6w6Pj+FL[(E]Pzu&s-3ScHI]Xb1WleLX_D";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("加载数据库驱动失败", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void close(Connection conn, Statement stmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
