package me.scarletleaf1000.sunworks.util;

import java.math.BigDecimal;

public class NumberUtil {

    /**
     * Converts large numbers into a human-readable format with a metric suffix (k, M, B, T).
     * For example: 3140 -> "3.14k"
     *
     * @param number The number to format
     * @return A formatted string representation of the number
     */
    public static String formatNumber(double number) {
        if (number < 0) {
            return "-" + formatNumber(-number);
        }
        if (number < 1000) {
            return formatValue(number) + "";
        }

        // Arrays to map the index to the correct metric suffix
        String[] suffixes = new String[]{"", "k", "M", "B", "T"};
        int exp = (int) (Math.log10(number) / 3);

        // Prevent ArrayOutOfBounds if the number exceeds the suffix array
        if (exp >= suffixes.length) {
            exp = suffixes.length - 1;
        }

        double shortValue = number / Math.pow(10, exp * 3);
        return formatValue(shortValue) + suffixes[exp];
    }

    // Helper method to strip trailing zeroes and format decimal places
    private static String formatValue(double value) {
        // Strip down to maximum 2 decimal places without trailing zeros
        if (value == (long) value) {
            return String.format("%d", (long) value);
        } else {
            return String.format("%.2f", value).replaceAll("0+$", "").replaceAll("\\.$", "");
        }
    }
}
