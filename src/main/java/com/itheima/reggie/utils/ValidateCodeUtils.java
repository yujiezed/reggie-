package com.itheima.reggie.utils;

import java.security.SecureRandom;

/** 验证码生成工具。验证码的有效期、保存和校验由业务层负责。 */
public final class ValidateCodeUtils {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] CHARACTERS = "0123456789abcdef".toCharArray();

    private ValidateCodeUtils() {
    }

    /**
     * 生成 4 位或 6 位数字验证码，首位不为零。
     *
     * @param length 验证码长度，只支持 4 或 6
     */
    public static Integer generateValidateCode(int length) {
        if (length == 4) {
            return 1000 + RANDOM.nextInt(9000);
        }
        if (length == 6) {
            return 100000 + RANDOM.nextInt(900000);
        }
        throw new IllegalArgumentException("只能生成4位或6位数字验证码");
    }

    /**
     * 生成指定长度的字符串验证码，字符范围与教程一致：0-9、a-f。
     *
     * @param length 正整数长度
     */
    public static String generateValidateCode4String(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("验证码长度必须大于0");
        }
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(CHARACTERS[RANDOM.nextInt(CHARACTERS.length)]);
        }
        return code.toString();
    }
}
