package com.movieticket.gongding.service;

import com.movieticket.gongding.dao.OrderDao;
import com.movieticket.gongding.entity.Order;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReminderService {
    private final OrderDao orderDao = new OrderDao();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String generateReminders(int userId) {
        try {
            List<Order> relevantOrders = orderDao.getUpcomingAndOngoingOrders(userId);
            if (relevantOrders.isEmpty()) {
                return "当前无待提醒的观影信息";
            }

            StringBuilder reminders = new StringBuilder();
            for (Order order : relevantOrders) {
                String message = buildReminderMessage(order);
                reminders.append(message).append("\n");
            }
            return reminders.toString();
        } catch (Exception e) {
            return "生成提醒时发生错误";
        }
    }

    private String buildReminderMessage(Order order) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime showStart = order.getShowTime();
        LocalDateTime showEnd = showStart.plusMinutes(order.getDuration());

        String status;
        if (now.isBefore(showStart)) {
            long minutes = Duration.between(now, showStart).toMinutes();
            status = String.format("【%s】即将开始，剩余时间：%d分钟", order.getMovieTitle(), minutes);
        } else if (now.isBefore(showEnd)) {
            long elapsed = Duration.between(showStart, now).toMinutes();
            status = String.format("【%s】正在放映，已播放：%d/%d分钟", order.getMovieTitle(), elapsed, order.getDuration());
        } else {
            return "";
        }

        return String.format("""
                        = 观影提醒 =
                        电影：%s
                        时间：%s
                        状态：%s
                        --------------------""",
                order.getMovieTitle(),
                showStart.format(TIME_FORMATTER),
                status
        );
    }
}
