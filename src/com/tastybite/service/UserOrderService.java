package com.tastybite.service;

import com.tastybite.dao.UserOrderDAO;
import com.tastybite.model.Account;
import com.tastybite.model.MyOrder;
import com.tastybite.model.Role;
import com.tastybite.util.Session;

import java.sql.SQLException;
import java.util.List;

/** Module B8: the customer's own order history and live status. */
public final class UserOrderService {

    /** The My Orders screen shows at most this many of the newest orders. */
    public static final int MAX_ORDERS = 100;

    private UserOrderService() { }

    /** The logged-in customer's orders (all restaurants), newest first. */
    public static List<MyOrder> getMyOrders() throws ServiceException {
        Account account = Session.getCurrent();
        if (account == null || account.role() != Role.USER) {
            throw new ServiceException("Only customers have a My Orders list.");
        }
        try {
            return UserOrderDAO.findByUser(account.id(), MAX_ORDERS);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServiceException("Could not load your orders. Check the database connection.", e);
        }
    }
}
