package com.tastybite.service;

import com.tastybite.dao.OrderDAO;
import com.tastybite.model.Account;
import com.tastybite.model.Bill;
import com.tastybite.model.Cart;
import com.tastybite.model.MenuItem;
import com.tastybite.model.Restaurant;
import com.tastybite.model.Role;
import com.tastybite.util.Session;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Module B4: bill calculation and placing the order. */
public final class OrderService {

    private OrderService() { }

    /** The bill for whatever is in the cart right now. */
    public static Bill currentBill() {
        return Bill.of(Session.getCart().subtotal());
    }

    /**
     * Places the order for the logged-in user from the session cart.
     * Before saving, the cart is compared with the live menu. If a price changed or an item was
     * removed by the manager, the cart is updated and the order is NOT placed, so the person never
     * pays a price they did not see.
     *
     * @return the new order id; the cart is emptied on success
     */
    public static int placeOrder() throws ServiceException {
        Account account = Session.getCurrent();
        Restaurant restaurant = Session.getRestaurant();
        Cart cart = Session.getCart();

        if (account == null || account.role() != Role.USER) {
            throw new ServiceException("Only customers can place orders.");
        }
        if (restaurant == null) {
            throw new ServiceException("Please choose a restaurant first.");
        }
        if (cart.isEmpty()) {
            throw new ServiceException("Your cart is empty.");
        }

        try {
            List<Integer> ids = new ArrayList<>();
            cart.items().forEach(ci -> ids.add(ci.item().id()));

            Map<Integer, MenuItem> current = OrderDAO.findCurrentItems(restaurant.id(), ids);
            if (cart.refresh(current)) {
                throw new ServiceException("Some items in your cart changed (price or availability). "
                        + "Your cart has been updated. Please review it and place the order again.");
            }

            int orderId = OrderDAO.insertOrder(account.id(), restaurant.id(),
                    Bill.of(cart.subtotal()), cart.items());
            cart.clear();
            return orderId;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServiceException("Could not place the order. Please try again.", e);
        }
    }
}
