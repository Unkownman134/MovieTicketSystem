package com.movieticket.gongding.controller;

import com.movieticket.gongding.dao.MovieDao;
import com.movieticket.gongding.entity.Movie;
import com.movieticket.gongding.entity.User;
import com.movieticket.gongding.service.MovieService;

import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class UserController {
    private final Scanner scanner = new Scanner(System.in);
    private final MovieService movieService = new MovieService();
    private final MovieDao movieDao = new MovieDao();

    public void showUserMenu(User user) {
        while (true) {
            System.out.println("=== 用户菜单 (" + user.getUsername() + ") ===");
            System.out.println("1. 查看电影列表");
            System.out.println("0. 退出登录");
            System.out.print("请选择操作：");

            switch (scanner.nextLine()) {
                case "1":
                    //获取电影表List，遍历输出
                    movieDao.getAllMovies().forEach(this::printMovieInfo);
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

    private void printMovieInfo(Movie movie) {
        System.out.printf("\n【%s】 时长：%d分钟\n", movie.getTitle(), movie.getDuration());
        System.out.printf("放映时间：%s\n", movie.getShowtime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        System.out.printf("剩余座位：%d/总座位：%d 票价：%.2f元\n", movie.getAvailableSeats(), movie.getTotalSeats(), movie.getPrice());
    }
}
