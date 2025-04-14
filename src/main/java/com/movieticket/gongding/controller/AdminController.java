package com.movieticket.gongding.controller;

import com.movieticket.gongding.service.MovieService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class AdminController {
    private final Scanner scanner = new Scanner(System.in);
    private final MovieService movieService = new MovieService();

    public void showAdminMenu() {
        while (true) {
            System.out.println("\n=== 管理员菜单 ===");
            System.out.println("1. 添加新电影");
            System.out.println("0. 退出登录");
            System.out.print("请选择操作：");

            switch (scanner.nextLine()) {
                case "1":
                    addMovie();
                    break;
                case "2":
                    break;
                case "3":
                    break;
                case "4":
                    break;
                case "0":
                    return;
                default:
                    System.out.println("无效选择！");
            }
        }
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
