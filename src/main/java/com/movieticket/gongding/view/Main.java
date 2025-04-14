package com.movieticket.gongding.view;

import com.movieticket.gongding.dao.UserDao;
import com.movieticket.gongding.service.UserService;

import java.util.Scanner;

public class Main {
    private final Scanner scanner = new Scanner(System.in);
    private final UserService userService = new UserService();

    public void start() {
        while (true) {
            System.out.println("\n=== 电影售票系统 ===");
            System.out.println("2. 用户注册");
            System.out.println("0. 退出系统");
            System.out.print("请选择操作：");

            switch (scanner.nextLine()) {
                case "1":
                    break;
                case "2":
                    userRegister();
                    break;
                case "3":
                    break;
                case "0":
                    System.out.println("再见！");
                    return;
                default:
                    System.out.println("无效选择，请重新输入！");
            }
        }
    }

    private void userRegister() {
        System.out.print("请输入用户名：");
        String username = scanner.nextLine();
        System.out.print("请输入密码：");
        String password = scanner.nextLine();
        System.out.print("请输入邮箱：");
        String email = scanner.nextLine();

        if (userService.register(username, password, email)) {
            System.out.println("注册成功！");
        } else {
            System.out.println("注册失败，用户名已存在！");
        }
    }

    public static void main(String[] args) {
        new Main().start();
    }
}