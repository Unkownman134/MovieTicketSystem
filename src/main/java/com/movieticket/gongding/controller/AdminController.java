package com.movieticket.gongding.controller;

import com.movieticket.gongding.dao.MovieDao;
import com.movieticket.gongding.dao.OrderDao;
import com.movieticket.gongding.dao.RefundRequestDao;
import com.movieticket.gongding.dao.UserDao;
import com.movieticket.gongding.entity.Movie;
import com.movieticket.gongding.entity.Order;
import com.movieticket.gongding.entity.RefundRequest;
import com.movieticket.gongding.entity.User;
import com.movieticket.gongding.service.MovieService;
import com.movieticket.gongding.service.UserService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class AdminController {
    private final Scanner scanner = new Scanner(System.in);
    private final MovieService movieService = new MovieService();
    private final MovieDao movieDao = new MovieDao();
    private final UserDao userDao = new UserDao();
    private final RefundRequestDao refundRequestDao = new RefundRequestDao();
    private final OrderDao orderDao = new OrderDao();

    public void showAdminMenu() {
        while (true) {
            System.out.println("\n=== 管理员菜单 ===");
            System.out.println("1. 添加新电影");
            System.out.println("2. 查看所有电影");
            System.out.println("3. 处理退票申请");
            System.out.println("4. 拉黑用户");
            System.out.println("0. 退出登录");
            System.out.print("请选择操作：");

            switch (scanner.nextLine()) {
                case "1":
                    addMovie();
                    break;
                case "2":
                    movieDao.getAllMovies().forEach(this::printMovieInfo);
                    break;
                case "3":
                    processRefund();
                    break;
                case "4":
                    blockUser();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("无效选择！");
            }
        }
    }

    private void processRefund() {
        // 获取待处理申请
        List<RefundRequest> requests = refundRequestDao.getAllPendingRequests();

        if (requests.isEmpty()) {
            System.out.println("\n当前没有待处理的退票申请");
            return;
        }

        // 显示申请列表
        System.out.println("\n=== 待处理退票申请 ===");
        System.out.printf("%-10s %-10s %-20s %-15s\n", "申请ID", "订单ID", "申请时间", "原因摘要");

        requests.forEach(req -> {
            String reasonPreview = req.getReason().length() > 15 ? req.getReason().substring(0, 15) + "..." : req.getReason();
            System.out.printf("%-10d %-10d %-20s %-15s\n", req.getId(), req.getOrderId(), req.getCreatedAt().format(DateTimeFormatter.ofPattern("MM-dd HH:mm")), reasonPreview);
        });

        try {
            System.out.print("\n请输入要处理的申请ID（0返回）：");
            int requestId = Integer.parseInt(scanner.nextLine());

            if (requestId == 0) {
                return;
            }

            // 获取申请详情
            RefundRequest request = refundRequestDao.getRequestDetails(requestId);
            if (request == null) {
                System.out.println("申请不存在！");
                return;
            }

            // 显示详情
            System.out.println("\n=== 申请详情 ===");
            System.out.println("订单ID：" + request.getOrderId());
            System.out.println("完整原因：" + request.getReason());
            System.out.println("申请时间：" + request.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

            // 处理选择
            System.out.print("是否同意退票（Y/N）：");
            boolean approve = "Y".equalsIgnoreCase(scanner.nextLine());

            System.out.print("处理意见：");
            String adminComment = scanner.nextLine();

            if (request == null) {
                System.out.println("申请不存在");
                return;
            }
            if (!"PENDING".equals(request.getStatus())) {
                System.out.println("申请已处理");
                return;
            }

            Order order = orderDao.getOrderById(request.getOrderId());
            if (order == null) {
                System.out.println("关联订单不存在");
                return;
            }

            Movie movie = movieDao.getMovieById(order.getMovieId());
            if (movie == null) {
                System.out.println("关联电影不存在");
                return;
            }

            String newStatus = approve ? "APPROVED" : "REJECTED";
            if (!refundRequestDao.updateRequestStatus(requestId, newStatus, adminComment)) {
                System.out.println("申请状态更新失败");
            }

            if (approve) {
                if (!movieDao.increaseSeats(movie.getId(), order.getSeatCount())) {
                    System.out.println("库存更新失败");
                    return;
                }
                if (!orderDao.updateOrderStatus(order.getId(), "REFUNDED")) {
                    System.out.println("订单状态更新失败");
                    return;
                }

                // 释放座位
                String refundSeats = order.getSeats();
                String currentSeats = movie.getSeats();
                String updatedSeats = currentSeats + "," + refundSeats;
                movieDao.updateMovieSeats(movie.getId(), updatedSeats);
            } else {
                if (!orderDao.updateOrderStatus(order.getId(), "PAID")) {
                    System.out.println("订单状态恢复失败");
                    return;
                }
            }
            System.out.println("处理完成：" + (approve ? "同意退票" : "拒绝退票"));
            return;
        } catch (NumberFormatException e) {
            System.out.println("输入格式错误，请输入数字ID！");
        }
    }

    private void blockUser() {
        try {
            List<User> users = userDao.getAllUsers();

            if (users.isEmpty()) {
                System.out.println("没有用户！");
            } else {
                System.out.println("\n=== 用户列表 ===");
                System.out.printf("%-5s %-15s %-10s %-20s\n", "ID", "用户名", "状态", "注册时间");
                users.forEach(this::printUserInfo);
            }

            System.out.print("\n请输入要拉黑的用户ID（0取消）：");
            int userId = Integer.parseInt(scanner.nextLine());

            if (userId == 0) {
                return;
            }

            boolean success = userDao.updateUserStatus(userId);
            System.out.println(success ? "操作成功！" : "用户不存在或操作失败！");
        } catch (SQLException e) {
            System.out.println("error");
        } catch (NumberFormatException e) {
            System.out.println("输入格式错误，请输入数字ID！");
        }
    }

    private void printUserInfo(User user) {
        String status = "NORMAL".equals(user.getStatus()) ? "正常" : "已拉黑";
        System.out.printf("%-5d %-15s %-10s %-20s\n", user.getId(), user.getUsername(), status, user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    }

    private void printMovieInfo(Movie movie) {
        System.out.printf("\nID：%d 【%s】版本：%d\n", movie.getId(), movie.getTitle(), movie.getVersion());
        System.out.printf("放映时间：%s 剩余座位：%d/%d\n", movie.getShowtime(), movie.getAvailableSeats(), movie.getTotalSeats());
    }

    private void addMovie() {
        try {
            System.out.print("电影标题：");
            String title = scanner.nextLine();
            System.out.print("电影描述：");
            String description = scanner.nextLine();
            System.out.print("放映时间（yyyy-MM-dd HH:mm）：");
            LocalDateTime showtime = LocalDateTime.parse(scanner.nextLine(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            System.out.print("时长（分钟）：");
            int duration = Integer.parseInt(scanner.nextLine());
            System.out.print("总座位数：");
            int seatsum = Integer.parseInt(scanner.nextLine());
            System.out.print("请输入票价：");
            BigDecimal price = new BigDecimal(scanner.nextLine());

            System.out.print("请输入座位号范围（格式：起始号-结束号，如1-50）：");
            String seatRange = scanner.nextLine();
            String[] range = seatRange.split("-");
            int start = Integer.parseInt(range[0]);
            int end = Integer.parseInt(range[1]);
            // 生成逗号分隔的座位号字符串
            StringBuilder seatsBuilder = new StringBuilder();
            for (int i = start; i <= end; i++) {
                seatsBuilder.append(i).append(",");
            }
            if (!seatsBuilder.isEmpty()) {
                // 删除最后一个逗号
                seatsBuilder.deleteCharAt(seatsBuilder.length() - 1);
            }
            String seats = seatsBuilder.toString();

            boolean success = movieService.addMovie(title, description, showtime, duration, seatsum, seats, price);
            System.out.println(success ? "添加成功！" : "添加失败！");
        } catch (NumberFormatException e) {
            System.out.println("价格格式错误，请输入正确数字（如：39.99）");
        } catch (Exception e) {
            System.out.println("输入格式错误！");
        }
    }
}
