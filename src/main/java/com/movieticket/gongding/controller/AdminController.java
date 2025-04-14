package com.movieticket.gongding.controller;

import com.movieticket.gongding.dao.MovieDao;
import com.movieticket.gongding.dao.UserDao;
import com.movieticket.gongding.entity.Movie;
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

    public void showAdminMenu() {
        while (true) {
            System.out.println("\n=== 管理员菜单 ===");
            System.out.println("1. 添加新电影");
            System.out.println("2. 查看所有电影");
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
            int seats = Integer.parseInt(scanner.nextLine());
            System.out.print("请输入票价：");
            BigDecimal price = new BigDecimal(scanner.nextLine());

            boolean success = movieService.addMovie(title, description, showtime, duration, seats, price);
            System.out.println(success ? "添加成功！" : "添加失败！");
        } catch (NumberFormatException e) {
            System.out.println("价格格式错误，请输入正确数字（如：39.99）");
        } catch (Exception e) {
            System.out.println("输入格式错误！");
        }
    }
}
