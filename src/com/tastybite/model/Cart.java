package com.tastybite.model;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The shopping cart. It lives in memory only and is saved to the database
 * as an order when the user clicks Place Order (Module F4/B4).
 */
public class Cart {

    private final Map<Integer, CartItem> items = new LinkedHashMap<>();

    public void add(MenuItem item) {
        items.computeIfAbsent(item.id(), id -> new CartItem(item)).increment();
    }

    /** The minus button: lowers the quantity by one and removes the line at zero. */
    public void decrement(int itemId) {
        CartItem ci = items.get(itemId);
        if (ci == null) return;
        if (ci.quantity() <= 1) {
            items.remove(itemId);
        } else {
            ci.decrement();
        }
    }

    public void remove(int itemId) { items.remove(itemId); }

    public List<CartItem> items() { return List.copyOf(items.values()); }

    public int totalQuantity() {
        int total = 0;
        for (CartItem ci : items.values()) total += ci.quantity();
        return total;
    }

    public BigDecimal subtotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (CartItem ci : items.values()) sum = sum.add(ci.lineTotal());
        return sum;
    }

    public boolean isEmpty() { return items.isEmpty(); }

    public void clear() { items.clear(); }
}
