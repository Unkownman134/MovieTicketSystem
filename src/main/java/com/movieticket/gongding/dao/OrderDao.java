package com.movieticket.gongding.dao;

import com.movieticket.gongding.entity.Order;
import com.movieticket.gongding.utils.JDBCUtils;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {
    public List<Order> getOrdersByUser(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT id, user_id, movie_id, seat_count, order_time, status FROM orders WHERE user_id = ?";
        try (Connection conn = JDBCUtils.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setUserId(rs.getInt("user_id"));
                order.setMovieId(rs.getInt("movie_id"));
                order.setSeatCount(rs.getInt("seat_count"));
                order.setStatus(rs.getString("status"));
                order.setOrderTime(rs.getTimestamp("order_time").toLocalDateTime());
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("获取订单列表失败: " + e.getMessage());
        }
        return orders;
    }

    public boolean creatOrder(Order order) {
        try (Connection conn = JDBCUtils.getConnection()) {
            String sql = "INSERT INTO orders (user_id, movie_id, seat_count, status, order_time, show_time, duration, movie_title, seats) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, order.getUserId());
                pstmt.setInt(2, order.getMovieId());
                pstmt.setInt(3, order.getSeatCount());
                pstmt.setString(4, order.getStatus());
                pstmt.setTimestamp(5, Timestamp.valueOf(order.getOrderTime()));
                pstmt.setTimestamp(6, Timestamp.valueOf(order.getShowTime()));
                pstmt.setInt(7, order.getDuration());
                pstmt.setString(8, order.getMovieTitle());
                pstmt.setString(9, order.getSeats());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet rs = pstmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            order.setId(rs.getInt(1));
                        }
                    }
                    return true;
                }
                return false;
            }
        } catch (SQLException e) {
            System.err.println("创建订单失败: " + e.getMessage());
        }
        return false;
    }

    public Order getOrderById(int orderId) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (Connection conn = JDBCUtils.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setUserId(rs.getInt("user_id"));
                    order.setMovieId(rs.getInt("movie_id"));
                    order.setSeatCount(rs.getInt("seat_count"));
                    order.setStatus(rs.getString("status"));
                    order.setSeats(rs.getString("seats"));
                    Timestamp orderTime = rs.getTimestamp("order_time");
                    if (orderTime == null) {
                        throw new SQLException("订单时间不可为空，订单ID: " + order.getId());
                    }
                    order.setOrderTime(orderTime.toLocalDateTime());
                    return order;
                }
                return null;
            }
        } catch (SQLException e) {
            System.err.println("获取订单列表失败: " + e.getMessage());
        }
        return null;
    }

    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, orderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Order> getUpcomingAndOngoingOrders(int userId) {
        LocalDateTime now = LocalDateTime.now();
        String sql = "SELECT o.*, m.showtime, m.duration, m.title AS movie_title " +
                "FROM orders o " +
                "JOIN movies m ON o.movie_id = m.id " +
                "WHERE o.user_id = ? " +
                "AND m.showtime <= DATE_ADD(NOW(), INTERVAL 10 MINUTE) " + // 10分钟内开始的或已开始的
                "AND DATE_ADD(m.showtime, INTERVAL m.duration MINUTE) > NOW()"; // 未结束的

        List<Order> orders = new ArrayList<>();
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Order order = new Order();
                order.setShowTime(rs.getTimestamp("showtime").toLocalDateTime());
                order.setDuration(rs.getInt("duration"));
                order.setMovieTitle(rs.getString("movie_title"));
                orders.add(order);
            }
        } catch (SQLException e) {
            System.err.println("查询待提醒订单失败：" + e.getMessage());
        }
        return orders;
    }
}
