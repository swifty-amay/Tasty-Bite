package com.tastybite.util;

import com.tastybite.model.Account;
import com.tastybite.model.Cart;
import com.tastybite.model.Restaurant;

/** Holds the logged-in account, the chosen restaurant and the cart. */
public final class Session {

    private static Account current;
    private static Restaurant restaurant;
    private static final Cart CART = new Cart();

    private Session() { }

    public static void login(Account account) {
        current = account;
        restaurant = account.restaurant(); // managers have one; users choose later
        CART.clear();
    }

    public static void logout() {
        current = null;
        restaurant = null;
        CART.clear();
    }

    public static Account getCurrent() { return current; }

    public static boolean isLoggedIn() { return current != null; }

    public static Restaurant getRestaurant() { return restaurant; }

    /** Choosing a different restaurant empties the cart, since items belong to one menu. */
    public static void setRestaurant(Restaurant r) {
        if (restaurant == null || r == null || restaurant.id() != r.id()) {
            CART.clear();
        }
        restaurant = r;
    }

    public static Cart getCart() { return CART; }
}
