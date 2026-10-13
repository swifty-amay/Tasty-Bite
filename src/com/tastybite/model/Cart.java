package com.tastybite.model;

import java.math.BigDecimal;
import java.util.ArrayList;
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

    /**
     * Brings the cart in line with the menu as it is right now.
     * Items that are no longer on the menu are removed; items whose name or price changed
     * are replaced (keeping the quantity). Returns true if anything changed.
     */
    public boolean refresh(Map<Integer, MenuItem> current) {
        boolean changed = false;
        for (Integer id : new ArrayList<>(items.keySet())) {
            CartItem ci = items.get(id);
            MenuItem now = current.get(id);
            if (now == null) {
                items.remove(id);
                changed = true;
            } else if (now.price().compareTo(ci.item().price()) != 0
                    || !now.name().equals(ci.item().name())) {
                items.put(id, new CartItem(now, ci.quantity()));
                changed = true;
            }
        }
        return changed;
    }

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
