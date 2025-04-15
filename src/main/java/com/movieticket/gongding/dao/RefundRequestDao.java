package com.movieticket.gongding.dao;

import com.movieticket.gongding.entity.Order;
import com.movieticket.gongding.entity.RefundRequest;
import com.movieticket.gongding.utils.JDBCUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RefundRequestDao {
    public List<Order> getRefundableOrders(int userId) {
        String sql = "SELECT o.* FROM orders o " +
                "JOIN movies m ON o.movie_id = m.id " +
                "WHERE o.user_id = ? " +
                "AND o.status = 'PAID' " +
                "AND m.showtime > NOW() + INTERVAL 1 HOUR"; // 放映前1小时可退票

        try (Connection conn = JDBCUtils.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            List<Order> orders = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setMovieId(rs.getInt("movie_id"));
                    order.setSeatCount(rs.getInt("seat_count"));
                    order.setStatus(rs.getString("status"));
                    order.setOrderTime(rs.getTimestamp("order_time").toLocalDateTime());
                    orders.add(order);
                }
            }
            return orders;
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public boolean createRequest(RefundRequest request) {
        String sql = "INSERT INTO refund_requests (order_id, reason) VALUES (?, ?)";
        try (Connection conn = JDBCUtils.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, request.getOrderId());
            pstmt.setString(2, request.getReason());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}
