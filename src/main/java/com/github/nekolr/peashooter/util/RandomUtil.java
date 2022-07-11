package com.github.nekolr.peashooter.util;


import java.util.Random;

public class RandomUtil {
    private static final char[] ORIGINAL_CHARS = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

    public static String generate(int length) {
        final int len = ORIGINAL_CHARS.length;
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(ORIGINAL_CHARS[random.nextInt(len)]);
        }
        return builder.toString();
    }
}
