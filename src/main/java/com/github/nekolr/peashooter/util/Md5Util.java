package com.github.nekolr.peashooter.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static com.github.nekolr.peashooter.constant.Peashooter.CHARSET;

public class Md5Util {

    private static final String MD5 = "MD5";
    private static final char[] ALPHABETS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static char[] encode(byte[] data) {
        int len = data.length;
        char[] out = new char[len << 1];
        int i = 0;
        for (int j = 0; i < len; ++i) {
            out[j++] = ALPHABETS[(240 & data[i]) >>> 4];
            out[j++] = ALPHABETS[15 & data[i]];
        }
        return out;
    }

    public static String md5(String content) {
        MessageDigest md;
        String dest;
        try {
            byte[] bytes = content.getBytes(CHARSET);
            md = MessageDigest.getInstance(MD5);
            md.update(bytes);
            dest = new String(encode(md.digest()));
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return dest;
    }
}
