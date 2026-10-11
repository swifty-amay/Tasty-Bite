package com.tastybite.model;

import java.math.BigDecimal;

/** One line of a saved order (name and price are the ones copied at order time). */
public record OrderLine(String name, BigDecimal unitPrice, int quantity) {

    public BigDecimal lineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
