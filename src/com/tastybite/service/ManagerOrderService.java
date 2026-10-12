package com.tastybite.service;

import com.tastybite.dao.OrderQueryDAO;
import com.tastybite.model.Account;
import com.tastybite.model.OrderDetail;
import com.tastybite.model.OrderStatus;
import com.tastybite.model.OrderSummary;
import com.tastybite.model.Restaurant;
import com.tastybite.model.Role;
import com.tastybite.util.Session;

import java.sql.SQLException;
import java.util.List;

/**
 * Modules B5 and B6: what the manager does with orders.
 * Dashboard: active orders (pending and preparing), View Order, Start Preparing, Mark Complete, Cancel.
 * History: every order with its status.
 * Completed and cancelled orders are never deleted; they only leave the dashboard list.
 *
 * Allowed moves:  PLACED -> PREPARING -> COMPLETED,  PLACED -> COMPLETED (skip preparing),
 *                 PLACED or PREPARING -> CANCELLED.
 */
public final class ManagerOrderService {

    /** The History screen shows at most this many of the newest orders. */
    public static final int MAX_HISTORY = 500;

    private static final List<OrderStatus> ACTIVE = List.of(OrderStatus.PLACED, OrderStatus.PREPARING);
    private static final List<OrderStatus> WAITING = List.of(OrderStatus.PLACED);

    private ManagerOrderService() { }

    public record HistoryResult(List<OrderSummary> orders, boolean truncated) { }

    /** Orders that still need attention (pending and preparing), newest first. */
    public static List<OrderSummary> getLatestOrders() throws ServiceException {
        Restaurant restaurant = requireManagerRestaurant();
        try {
            return OrderQueryDAO.findByStatuses(restaurant.id(), ACTIVE);
        } catch (SQLException e) {
            throw loadError(e);
        }
    }

    /** All orders (pending, completed, cancelled), newest first. */
    public static HistoryResult getHistory() throws ServiceException {
        Restaurant restaurant = requireManagerRestaurant();
        try {
            List<OrderSummary> rows = OrderQueryDAO.findAll(restaurant.id(), MAX_HISTORY + 1);
            boolean truncated = rows.size() > MAX_HISTORY;
            return new HistoryResult(truncated ? rows.subList(0, MAX_HISTORY) : rows, truncated);
        } catch (SQLException e) {
            throw loadError(e);
        }
    }

    public static OrderDetail getDetail(int orderId) throws ServiceException {
        Restaurant restaurant = requireManagerRestaurant();
        try {
            return OrderQueryDAO.findDetail(orderId, restaurant.id())
                    .orElseThrow(() -> new ServiceException("That order could not be found."));
        } catch (SQLException e) {
            throw loadError(e);
        }
    }

    /** The start button: a pending order is now being prepared. It stays on the dashboard. */
    public static void startPreparing(int orderId) throws ServiceException {
        changeStatus(orderId, WAITING, OrderStatus.PREPARING,
                "This order is not waiting any more. It may already be in preparation, completed or cancelled.");
    }

    /** The tick button: the order is done. It leaves the dashboard but stays in History. */
    public static void markCompleted(int orderId) throws ServiceException {
        changeStatus(orderId, ACTIVE, OrderStatus.COMPLETED, FINISHED_MESSAGE);
    }

    /** The cross button: the order is cancelled. It leaves the dashboard but stays in History. */
    public static void cancelOrder(int orderId) throws ServiceException {
        changeStatus(orderId, ACTIVE, OrderStatus.CANCELLED, FINISHED_MESSAGE);
    }

    private static final String FINISHED_MESSAGE =
            "This order has already been completed or cancelled, or does not exist.";

    private static void changeStatus(int orderId, List<OrderStatus> from, OrderStatus to, String refusal)
            throws ServiceException {
        Restaurant restaurant = requireManagerRestaurant();
        try {
            int changed = OrderQueryDAO.updateStatus(orderId, restaurant.id(), from, to);
            if (changed == 0) {
                throw new ServiceException(refusal);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServiceException("Could not update the order. Please try again.", e);
        }
    }

    private static Restaurant requireManagerRestaurant() throws ServiceException {
        Account account = Session.getCurrent();
        if (account == null || account.role() != Role.MANAGER || account.restaurant() == null) {
            throw new ServiceException("Only a restaurant manager can see orders.");
        }
        return account.restaurant();
    }

    private static ServiceException loadError(SQLException e) {
        e.printStackTrace();
        return new ServiceException("Could not load orders. Check the database connection.", e);
    }
}
