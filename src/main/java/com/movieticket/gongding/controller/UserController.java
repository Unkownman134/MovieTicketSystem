package com.movieticket.gongding.controller;

import com.movieticket.gongding.entity.User;

import java.util.Scanner;

public class UserController {
    private final Scanner scanner = new Scanner(System.in);

    public void showUserMenu(User user) {
        while (true) {
            System.out.println("=== 用户菜单 (" + user.getUsername() + ") ===");
            System.out.println("0. 退出登录");
            System.out.print("请选择操作：");

            switch (scanner.nextLine()) {
                case "1":
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
}
