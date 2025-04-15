package com.movieticket.gongding.dao;

import com.movieticket.gongding.entity.Order;
import com.movieticket.gongding.entity.RefundRequest;
import com.movieticket.gongding.utils.JDBCUtils;

import java.sql.*;
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

    public List<RefundRequest> getAllPendingRequests() {
        String sql = "SELECT * FROM refund_requests WHERE status = 'PENDING'";
        try (Connection conn = JDBCUtils.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            List<RefundRequest> requests = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    RefundRequest request = new RefundRequest();
                    request.setId(rs.getInt("id"));
                    request.setOrderId(rs.getInt("order_id"));
                    request.setReason(rs.getString("reason"));
                    request.setStatus(rs.getString("status"));
                    request.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    request.setProcessedAt(rs.getTimestamp("processed_at") != null ? rs.getTimestamp("processed_at").toLocalDateTime() : null);
                    request.setAdminComment(rs.getString("admin_comment"));
                }
            }
            return requests;
        } catch (SQLException e) {
            return Collections.emptyList();
        }
    }

    public RefundRequest getRequestDetails(int requestId) {
        try (Connection conn = JDBCUtils.getConnection()) {
            String sql = "SELECT * FROM refund_requests WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, requestId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        RefundRequest request = new RefundRequest();
                        request.setId(rs.getInt("id"));
                        request.setOrderId(rs.getInt("order_id"));
                        request.setReason(rs.getString("reason"));
                        request.setStatus(rs.getString("status"));
                        request.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                        request.setProcessedAt(rs.getTimestamp("processed_at") != null ? rs.getTimestamp("processed_at").toLocalDateTime() : null);
                        request.setAdminComment(rs.getString("admin_comment"));
                    }
                }
            }
        } catch (SQLException e) {
            return null;
        }
        return null;
    }

    public RefundRequest getRequestById(int requestId) {
        try (Connection conn = JDBCUtils.getConnection()) {
            String sql = "SELECT * FROM refund_requests WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, requestId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        RefundRequest request = new RefundRequest();
                        request.setId(rs.getInt("id"));
                        request.setOrderId(rs.getInt("order_id"));
                        request.setReason(rs.getString("reason"));
                        request.setStatus(rs.getString("status"));
                        request.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                        request.setProcessedAt(rs.getTimestamp("processed_at") != null ? rs.getTimestamp("processed_at").toLocalDateTime() : null);
                        request.setAdminComment(rs.getString("admin_comment"));
                    }
                }
            }
        } catch (SQLException e) {
            return null;
        }
        return null;
    }

    public boolean updateRequestStatus(int requestId, String status, String adminComment) {
        String sql = "UPDATE refund_requests SET status = ?, processed_at = NOW(), admin_comment = ? WHERE id = ?";
        try (Connection conn = JDBCUtils.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, adminComment);
            pstmt.setInt(3, requestId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}
