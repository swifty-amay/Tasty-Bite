package com.tastybite.service;

import com.tastybite.dao.MenuDAO;
import com.tastybite.model.MenuItem;
import com.tastybite.model.MenuSection;
import com.tastybite.model.Restaurant;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Module B2: what the user sees when choosing a restaurant and browsing its menu. */
public final class MenuService {

    private MenuService() { }

    public static List<Restaurant> getRestaurants() throws ServiceException {
        try {
            return MenuDAO.findRestaurants();
        } catch (SQLException e) {
            throw new ServiceException("Could not load restaurants. Check the database connection.", e);
        }
    }

    /** The menu for customers: only categories that have at least one item. */
    public static List<MenuSection> getMenu(int restaurantId) throws ServiceException {
        try {
            List<MenuSection> visible = new ArrayList<>();
            for (MenuSection s : MenuDAO.findMenu(restaurantId)) {
                if (!s.items().isEmpty()) visible.add(s);
            }
            return visible;
        } catch (SQLException e) {
            throw new ServiceException("Could not load the menu. Check the database connection.", e);
        }
    }

    /** Adds one of the item to the in-memory cart of the current session. */
    public static void addToCart(MenuItem item) {
        com.tastybite.util.Session.getCart().add(item);
    }
}
