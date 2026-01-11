package com.github.nekolr.peashooter.util;

import me.xdrop.fuzzywuzzy.FuzzySearch;

public class FuzzUtil {

    private static final int SIMILARITY_THRESHOLD = 90; // 相似度阈值，0-100

    /**
     * 判断两个字符串是否相似
     *
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return 如果相似则返回 true，否则返回 false
     */
    public static boolean isSimilar(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return false;
        }
        return FuzzySearch.ratio(str1, str2) >= SIMILARITY_THRESHOLD;
    }
}
