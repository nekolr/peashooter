package com.github.nekolr.peashooter.util;

import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 磁力链接工具类
 */
public class MagnetUtils {

    private static final String MAGNET_PREFIX = "magnet:";

    /**
     * 匹配 btih 的 info hash，兼容 urn:btih: 前缀。参数名与命名空间大小写不敏感
     */
    private static final Pattern BTIH_PATTERN = Pattern.compile("xt=urn:btih:([A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE);

    /**
     * 40 位十六进制 hash
     */
    private static final Pattern HEX_40_PATTERN = Pattern.compile("[0-9a-fA-F]{40}");

    /**
     * base32 字母表（RFC 4648）
     */
    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    /**
     * BitTorrent v1 info hash 长度（字节）
     */
    private static final int INFO_HASH_BYTES = 20;

    private MagnetUtils() {
    }

    /**
     * 是否是磁力链接
     */
    public static boolean isMagnet(String url) {
        return url != null && url.regionMatches(true, 0, MAGNET_PREFIX, 0, MAGNET_PREFIX.length());
    }

    /**
     * 提取 info hash，统一转换为 40 位小写的十六进制字符串
     *
     * @param url 磁力链接
     * @return 40 位小写 hex，无法解析时返回 null
     */
    public static String extractInfoHash(String url) {
        if (!isMagnet(url)) {
            return null;
        }
        String hash = findBtihHash(url);
        if (hash == null) {
            // 兼容整体被 URL 编码的磁力链接，如 xt=urn%3Abtih%3A...
            try {
                hash = findBtihHash(URLDecoder.decode(url, StandardCharsets.UTF_8));
            } catch (Exception ignored) {
                // 解码失败则按无法解析处理
            }
        }
        return normalize(hash);
    }

    private static String findBtihHash(String url) {
        if (url == null) {
            return null;
        }
        Matcher matcher = BTIH_PATTERN.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String normalize(String hash) {
        if (hash == null) {
            return null;
        }
        if (HEX_40_PATTERN.matcher(hash).matches()) {
            return hash.toLowerCase(Locale.ROOT);
        }
        // BitTorrent v2 使用 32 字节 hash（52 位 base32 或 64 位 hex），此处不支持，返回 null
        byte[] bytes = decodeBase32(hash);
        if (bytes != null && bytes.length == INFO_HASH_BYTES) {
            return toHex(bytes);
        }
        return null;
    }

    private static String toHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(Character.forDigit((b >> 4) & 0xF, 16));
            hex.append(Character.forDigit(b & 0xF, 16));
        }
        return hex.toString();
    }

    /**
     * 解码 base32（无填充）
     */
    private static byte[] decodeBase32(String input) {
        String upper = input.toUpperCase(Locale.ROOT);
        int buffer = 0;
        int bitsLeft = 0;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < upper.length(); i++) {
            int value = BASE32_ALPHABET.indexOf(upper.charAt(i));
            if (value < 0) {
                return null;
            }
            buffer = (buffer << 5) | value;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                out.write((buffer >> bitsLeft) & 0xFF);
            }
        }
        return out.toByteArray();
    }
}
