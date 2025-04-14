package com.movieticket.gongding.service;

import com.movieticket.gongding.dao.MovieDao;
import com.movieticket.gongding.dao.OrderDao;
import com.movieticket.gongding.dao.UserDao;
import com.movieticket.gongding.entity.Movie;
import com.movieticket.gongding.entity.Order;
import com.movieticket.gongding.entity.User;
import com.movieticket.gongding.utils.PasswordUtils;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class UserService {
    private final Scanner scanner = new Scanner(System.in);
    private final UserDao userDao = new UserDao();
    private final MovieDao movieDao = new MovieDao();
    private final OrderDao orderDao = new OrderDao();
    //最大购票尝试次数
    private static final int MAX_RETRY = 3;
    //停止售票提前时间
    private static final int MAX_PLUS_TIME = 10;

    //用户注册服务
    public boolean register(String username, String passwordHash, String email) {
        try {
            //注册用户名已存在
            if (userDao.findUserByUsername(username) != null) {
                return false;
            }

            String salt = PasswordUtils.generateSalt();
            String hashedPassword = PasswordUtils.hashPassword(passwordHash, salt);

            User user = new User();
            user.setUsername(username);
            user.setPasswordHash(hashedPassword);
            user.setEmail(email);
            user.setSalt(salt);

            return userDao.addUser(user);
        } catch (Exception e) {
            return false;
        }
    }

    public int login(String username, String password) {
        try {
            User user = userDao.findUserByUsername(username);
            if (user == null) {
                //用户不存在
                return -1;
            }
            if ("BLACKLIST".equals(user.getStatus())) {
                //用户被拉黑
                return -2;
            }

            String calculatedHash = PasswordUtils.hashPassword(password, user.getSalt());
            if (!calculatedHash.equals(user.getPasswordHash())) {
                //密码错误
                return -3;
            }

            //更新用户登录时间
            userDao.updateLoginTime(user.getId());
            //登录成功
            return 1;
        } catch (Exception e) {
            //系统错误
            return -4;
        }
    }

    public void purchaseTicket(int userId) {
        List<Movie> movies = movieDao.getAllMovies();
        if (movies.isEmpty()) {
            System.out.println("当前没有电影！");
            return;
        }

        //获取可购买电影List
        System.out.println("\n=== 可购票电影 ===");
        movies.forEach(movie -> {
            System.out.printf("ID：%-4d %-20s 剩余座位：%d/%d 票价：%.2f元\n", movie.getId(), movie.getTitle(), movie.getAvailableSeats(), movie.getTotalSeats(), movie.getPrice());
        });

        //购票
        try {
            System.out.print("\n请输入电影ID：");
            int movieId = Integer.parseInt(scanner.nextLine());
            System.out.print("请输入购买座位数：");
            int seats = Integer.parseInt(scanner.nextLine());

            //尝试购票
            for (int retry = 0; retry < MAX_RETRY; retry++) {
                try {
                    Movie movie = movieDao.getMovieById(movieId);
                    if (movie == null) {
                        System.out.println("电影不存在！");
                        return;
                    }
                    if (movie.getAvailableSeats() < seats) {
                        System.out.println("剩余座位不足！");
                        return;
                    }
                    if (movie.getShowtime().isBefore(LocalDateTime.now().plusMinutes(MAX_PLUS_TIME))) {
                        System.out.println("距离电影放映不足10分钟，停止售票！");
                        return;
                    }

                    //解决高并发冲突
                    boolean success = movieDao.decreaseSeatsWithVersion(movieId, seats, movie.getVersion());

                    if (!success) {
                        if (retry == MAX_RETRY - 1) {
                            System.out.println("系统繁忙，请稍后重试！");
                            return;
                        }
                        continue;
                    }

                    Order order = new Order();
                    order.setUserId(userId);
                    order.setMovieId(movieId);
                    order.setSeatCount(seats);
                    order.setStatus("PAID");
                    order.setShowTime(movie.getShowtime());
                    order.setDuration(movie.getDuration());
                    order.setMovieTitle(movie.getTitle());
                    order.setOrderTime(LocalDateTime.now());

                    if (!orderDao.creatOrder(order)) {
                        System.out.println("订单创建失败!");
                        return;
                    }
                    System.out.println("成功购票！订单号：" + order.getId());
                    return;
                } catch (SQLException e) {
                    if (retry == MAX_RETRY - 1) {
                        System.out.println("系统错误！" + e.getMessage());
                        return;
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("输入格式错误！");
        }
    }
}
