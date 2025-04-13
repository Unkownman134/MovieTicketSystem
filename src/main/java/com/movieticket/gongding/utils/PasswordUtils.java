package com.movieticket.gongding.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

//借助网络资料，学习了SHA-256加密方法
public class PasswordUtils {
    //指定哈希算法为SHA-256
    private static final String HASH_ALGORITHM = "SHA-256";
    //设置盐值，防止彩虹表攻击
    private static final int SALT_LENGTH = 16;

    //生成随机盐值，返回十六进制字符串
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return bytesToHex(salt);
    }

    //将密码与盐值做哈希，返回十六进制字符串
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            //在哈希计算前混入盐值
            md.update(hexToBytes(salt));
            //完成最后的哈希计算
            byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("不支持的安全算法：" + HASH_ALGORITHM, e);
        }
    }

    //将字节数组转换成十六进制字符串
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            //将字节转换为无符号整数
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    //将十六进制字符串还原成字节数组
    private static byte[] hexToBytes(String hex) {
        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length() / 2; i++) {
            int high = Integer.parseInt(hex.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hex.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }
}
