package com.github.nekolr.peashooter.util;

import me.xdrop.fuzzywuzzy.FuzzySearch;

public class FuzzUtil {

    private static final int SIMILARITY_THRESHOLD = 85; // 相似度阈值，0-100

    /**
     * 计算两个字符串的相似度得分。
     * 使用四种算法取最高分：
     * - ratio：整体字符串相似度
     * - partialRatio：子串匹配（处理 "SPY×FAMILY" vs "SPY×FAMILY Season 2"）
     * - tokenSortRatio：忽略词序（处理 "Family Spy" vs "Spy Family"）
     * - tokenSetRatio：忽略额外词（处理 "SPY×FAMILY" vs "SPY×FAMILY [1080p]"）
     *
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return 相似度得分，0-100
     */
    public static int score(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return 0;
        }
        return Math.max(
                Math.max(FuzzySearch.ratio(str1, str2),
                        FuzzySearch.partialRatio(str1, str2)),
                Math.max(FuzzySearch.tokenSortRatio(str1, str2),
                        FuzzySearch.tokenSetRatio(str1, str2))
        );
    }

    /**
     * 判断两个字符串是否相似
     *
     * @param str1 第一个字符串
     * @param str2 第二个字符串
     * @return 如果相似则返回 true，否则返回 false
     */
    public static boolean isSimilar(String str1, String str2) {
        return score(str1, str2) >= SIMILARITY_THRESHOLD;
    }
}
