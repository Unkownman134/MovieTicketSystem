package com.movieticket.gongding.service;

import com.movieticket.gongding.dao.MovieDao;
import com.movieticket.gongding.dao.OrderDao;
import com.movieticket.gongding.dao.RefundRequestDao;
import com.movieticket.gongding.dao.UserDao;
import com.movieticket.gongding.entity.Movie;
import com.movieticket.gongding.entity.Order;
import com.movieticket.gongding.entity.RefundRequest;
import com.movieticket.gongding.entity.User;
import com.movieticket.gongding.utils.PasswordUtils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class UserService {
    private final Scanner scanner = new Scanner(System.in);
    private final UserDao userDao = new UserDao();
    private final MovieDao movieDao = new MovieDao();
    private final OrderDao orderDao = new OrderDao();
    private final RefundRequestDao refundRequestDao = new RefundRequestDao();
    //最大购票尝试次数  
    private static final int MAX_RETRY = 3;
    //停止售票提前时间
    static final int MAX_PLUS_TIME = 5;
    //停止退票提前时间
    private static final int MAX_REFUND_PLUS_TIME = 5;

    //用户注册服务
    public boolean register(String username, String passwordHash, String email, BigDecimal money) {
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
            user.setMoney(money);

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

    public void purchaseTicket(int userId, String username, BigDecimal money) {
        List<Movie> movies = movieDao.getAllMovies();
        User user = userDao.findUserByUsername(username);

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
            int seatsum = Integer.parseInt(scanner.nextLine());

            //尝试购票
            for (int retry = 0; retry < MAX_RETRY; retry++) {
                Movie movie = movieDao.getMovieById(movieId);
                if (movie == null) {
                    System.out.println("电影不存在！");
                    return;
                }
                if (movie.getAvailableSeats() < seatsum) {
                    System.out.println("剩余座位不足！");
                    return;
                }
                if (movie.getShowtime().isBefore(LocalDateTime.now().plusMinutes(MAX_PLUS_TIME))) {
                    System.out.println("距离电影放映不足5分钟，停止售票！");
                    return;
                }
                BigDecimal totalPrice = movie.getPrice().multiply(BigDecimal.valueOf(seatsum));
                if (user.getMoney().compareTo(totalPrice) < 0) {
                    System.out.println("余额不足！");
                    return;
                }

                // 显示可用座位
                String[] availableSeats = movie.getSeats().split(",");
                System.out.println("可用座位：" + Arrays.toString(availableSeats));
                // 用户选座
                System.out.print("请输入要购买的座位号（用逗号分隔，如1,2）：");
                String selectedSeats = scanner.nextLine();
                String[] seats = selectedSeats.split(",");
                // 验证座位是否可用
                Set<String> availableSet = new HashSet<>(Arrays.asList(availableSeats));
                for (String seat : seats) {
                    if (!availableSet.contains(seat.trim())) {
                        System.out.println("座位 " + seat + " 不可选！");
                        return;
                    }
                }

                //解决高并发冲突
                boolean success = movieDao.decreaseSeatsWithVersion(movieId, seatsum, movie.getVersion());

                if (!success) {
                    if (retry == MAX_RETRY - 1) {
                        System.out.println("系统繁忙，请稍后重试！");
                        return;
                    }
                    continue;
                }

                // 更新电影可用座位
                List<String> remainingSeats = new ArrayList<>(availableSet);
                remainingSeats.removeAll(Arrays.asList(seats));
                String newSeats = String.join(",", remainingSeats);
                if (!movieDao.updateMovieSeats(movieId, newSeats)) {
                    System.out.println("选座失败！");
                    return;
                }

                userDao.updateUserMoney(userId,totalPrice,"SUB");

                Order order = new Order();
                order.setUserId(userId);
                order.setMovieId(movieId);
                order.setSeatCount(seatsum);
                order.setStatus("PAID");
                order.setShowTime(movie.getShowtime());
                order.setDuration(movie.getDuration());
                order.setMovieTitle(movie.getTitle());
                order.setOrderTime(LocalDateTime.now());
                order.setSeats(selectedSeats);

                if (!orderDao.creatOrder(order)) {
                    System.out.println("订单创建失败!");
                    return;
                }
                System.out.println("成功购票！订单号：" + order.getId());
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("输入格式错误！");
        } catch (SQLException e) {
            System.out.println("系统异常！");
        }
    }

    public void applyRefund(int userId) {
        List<Order> refundableOrders = refundRequestDao.getRefundableOrders(userId);

        if (refundableOrders.isEmpty()) {
            System.out.println("您当前没有可退票的订单！");
            return;
        }

        // 显示订单列表
        System.out.println("\n=== 可退票订单 ===");
        System.out.printf("%-10s %-15s %-10s %-20s\n", "订单号", "电影名称", "座位数", "下单时间");

        refundableOrders.forEach(order -> {
            Movie movie = movieDao.getMovieById(order.getMovieId());
            System.out.printf("%-10d %-15s %-10d %-20s\n", order.getId(), movie.getTitle(), order.getSeatCount(), order.getOrderTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            );
        });

        try {
            System.out.print("\n请输入要退款的订单号（0返回）：");
            int orderId = Integer.parseInt(scanner.nextLine());

            if (orderId == 0) {
                return;
            }

            // 验证订单是否有效
            boolean isValid = false;
            for (Order o : refundableOrders) {
                if (o.getId() == orderId) {
                    isValid = true;
                    break;
                }
            }

            if (!isValid) {
                System.out.println("订单号无效！");
                return;
            }

            System.out.print("请输入退款原因：");
            String reason = scanner.nextLine();

            Order order = orderDao.getOrderById(orderId);
            if (order == null) {
                System.out.println("订单不存在");
                return;
            }

            if (order.getUserId() != userId) {
                System.out.println("无权操作此订单");
                return;
            }
            if (!"PAID".equals(order.getStatus())) {
                System.out.println("当前状态不可退票");
                return;
            }

            Movie movie = movieDao.getMovieById(order.getMovieId());
            if (movie.getShowtime().isBefore(LocalDateTime.now().plusMinutes(MAX_REFUND_PLUS_TIME))) {
                System.out.println("已超过退票截止时间，电影开始前5分钟截至退票！");
                return;
            }

            RefundRequest request = new RefundRequest();
            request.setOrderId(orderId);
            request.setReason(reason);
            request.setUserId(userId);

            BigDecimal totalPrice=movie.getPrice().multiply(new BigDecimal(order.getSeatCount()));
            request.setMoney(totalPrice);

            if (!refundRequestDao.createRequest(request)) {
                System.out.println("退票申请提交失败");
                return;
            }
            if (!orderDao.updateOrderStatus(orderId, "REFUNDING")) {
                System.out.println("状态更新失败");
                return;
            }

            System.out.println("退票申请已提交，等待审核");
        } catch (NumberFormatException e) {
            System.out.println("输入格式错误！");
        }
    }
}
