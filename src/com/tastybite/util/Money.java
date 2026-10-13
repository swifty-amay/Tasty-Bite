package com.tastybite.util;

import java.math.BigDecimal;

/** One place to change the currency symbol and format. */
public final class Money {

    private static final String SYMBOL = "\u20B9"; // rupee sign

    private Money() { }

    public static String format(BigDecimal amount) {
        return String.format("%s%,.2f", SYMBOL, amount);
    }
}
