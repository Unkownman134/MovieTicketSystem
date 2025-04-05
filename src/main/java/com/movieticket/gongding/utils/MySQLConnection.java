package com.movieticket.gongding.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//测试数据库连接
public class MySQLConnection {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/movie_ticket_system_db?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=UTF-8";
        String user = "root";
        String password = "^T.GKiPl({xfLt0M6w6Pj+FL[(E]Pzu&s-3ScHI]Xb1WleLX_D";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Success connect to MySQL Server");

            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Success Connection Established");
            conn.close();
            System.out.println("Connection Closed Successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("Error loading JDBC driver " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error Connection to MySQL Database " + e.getMessage());
        }
    }
}
