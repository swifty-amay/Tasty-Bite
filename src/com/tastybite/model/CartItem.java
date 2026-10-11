package com.tastybite.model;

import java.math.BigDecimal;

/** A menu item in the cart with its quantity. */
public class CartItem {

    private final MenuItem item;
    private int quantity;

    CartItem(MenuItem item) {
        this(item, 0);
    }

    CartItem(MenuItem item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public MenuItem item() { return item; }

    public int quantity() { return quantity; }

    void increment() { quantity++; }

    void decrement() { quantity--; }

    public BigDecimal lineTotal() {
        return item.price().multiply(BigDecimal.valueOf(quantity));
    }
}
