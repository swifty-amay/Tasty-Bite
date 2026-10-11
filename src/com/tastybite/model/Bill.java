package com.tastybite.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Subtotal, tax and total. The cart screen and the saved order both use Bill.of(), so they always agree. */
public record Bill(BigDecimal subtotal, BigDecimal tax, BigDecimal total) {

    /** Tax rate applied to every order (5%). Change it here. */
    public static final BigDecimal TAX_RATE = new BigDecimal("0.05");

    public static Bill of(BigDecimal subtotal) {
        BigDecimal sub = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal tax = sub.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        return new Bill(sub, tax, sub.add(tax));
    }

    /** For labels such as "Tax (5%)". */
    public static String taxPercentLabel() {
        return TAX_RATE.movePointRight(2).stripTrailingZeros().toPlainString() + "%";
    }
}
