package com.movieticket.gongding.controller;

import com.movieticket.gongding.dao.MovieDao;
import com.movieticket.gongding.dao.OrderDao;
import com.movieticket.gongding.entity.Movie;
import com.movieticket.gongding.entity.Order;
import com.movieticket.gongding.entity.User;
import com.movieticket.gongding.service.UserService;

import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class UserController {
    private final Scanner scanner = new Scanner(System.in);
    private final MovieDao movieDao = new MovieDao();
    private final OrderDao orderDao = new OrderDao();
    private final UserService userService = new UserService();

    public void showUserMenu(User user) {
        while (true) {
            System.out.println("=== 用户菜单 (" + user.getUsername() + ") ===");
            System.out.println("1. 查看电影列表");
            System.out.println("2. 购买电影票");
            System.out.println("3. 查看我的订单");
            System.out.println("0. 退出登录");
            System.out.print("请选择操作：");

            switch (scanner.nextLine()) {
                case "1":
                    //获取电影表List，遍历输出
                    movieDao.getAllMovies().forEach(this::printMovieInfo);
                    break;
                case "2":
                    userService.purchaseTicket(user.getId());
                    break;
                case "3":
                    orderDao.getOrdersByUser(user.getId()).forEach(this::printOrderInfo);
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

    private void printOrderInfo(Order order) {
        System.out.printf("\n订单号：%d 状态：%s\n", order.getId(), order.getStatus());
        System.out.printf("购买时间：%s 座位数：%d\n", order.getOrderTime(), order.getSeatCount());
    }

    private void printMovieInfo(Movie movie) {
        System.out.printf("\n【%s】 时长：%d分钟\n", movie.getTitle(), movie.getDuration());
        System.out.printf("放映时间：%s\n", movie.getShowtime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        System.out.printf("剩余座位：%d 总座位：%d 票价：%.2f元\n", movie.getAvailableSeats(), movie.getTotalSeats(), movie.getPrice());
    }
}
