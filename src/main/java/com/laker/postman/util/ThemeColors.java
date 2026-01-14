package com.laker.postman.util;

import java.awt.Color;

public final class ThemeColors {
    private ThemeColors() {}

    private static String toHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }

    public static String getBorderColor() {
        // 使用现有主题颜色源的边框颜色
        Color c = com.laker.postman.common.constants.ModernColors.getDividerBorderColor();
        return toHex(c);
    }

    public static String getTextColor() {
        Color c = com.laker.postman.common.constants.ModernColors.getTextPrimary();
        return toHex(c);
    }

    public static String getSecondaryTextColor() {
        Color c = com.laker.postman.common.constants.ModernColors.getTextSecondary();
        return toHex(c);
    }

    public static String getHintTextColor() {
        Color c = com.laker.postman.common.constants.ModernColors.getTextHint();
        return toHex(c);
    }

    public static String getLinkColor() {
        return com.laker.postman.common.themes.SimpleThemeManager.isDarkTheme() ? "#60a5fa" : "#1a0dab";
    }
}
