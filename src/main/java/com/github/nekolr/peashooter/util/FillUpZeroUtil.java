package com.github.nekolr.peashooter.util;

public class FillUpZeroUtil {

    public static String fill(String numStr) {
        return String.format("%02d", Integer.valueOf(numStr));
    }
}
