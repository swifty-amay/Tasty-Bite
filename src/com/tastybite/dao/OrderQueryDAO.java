package com.tastybite.dao;

import com.tastybite.model.OrderDetail;
import com.tastybite.model.OrderLine;
import com.tastybite.model.OrderStatus;
import com.tastybite.model.OrderSummary;
import com.tastybite.util.DBConnection;
import com.tastybite.util.DbTime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Modules B5 and B6: reading and updating a restaurant's orders.
 * Every statement is limited to one restaurant_id, so a manager only ever sees their own orders.
 */
public final class OrderQueryDAO {

    private static final String SUMMARY_SELECT =
            "SELECT o.order_id, o.user_id, u.username, o.total, o.status, "
                    + "UNIX_TIMESTAMP(o.placed_at) AS placed_epoch, UNIX_TIMESTAMP(o.updated_at) AS updated_epoch "
                    + "FROM orders o JOIN users u ON u.user_id = o.user_id "
                    + "WHERE o.restaurant_id = ? ";

    private OrderQueryDAO() { }

    /** Orders in any of the given statuses, newest first (the dashboard uses PLACED and PREPARING). */
    public static List<OrderSummary> findByStatuses(int restaurantId, Collection<OrderStatus> statuses)
            throws SQLException {
        if (statuses.isEmpty()) return new ArrayList<>();
        String sql = SUMMARY_SELECT + "AND o.status IN (" + placeholders(statuses.size())
                + ") ORDER BY o.placed_at DESC, o.order_id DESC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            int i = 2;
            for (OrderStatus st : statuses) ps.setString(i++, st.name());
            return readSummaries(ps);
        }
    }

    /** Every order of the restaurant, newest first, at most "limit" rows. */
    public static List<OrderSummary> findAll(int restaurantId, int limit) throws SQLException {
        String sql = SUMMARY_SELECT + "ORDER BY o.placed_at DESC, o.order_id DESC LIMIT ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            ps.setInt(2, limit);
            return readSummaries(ps);
        }
    }

    public static Optional<OrderDetail> findDetail(int orderId, int restaurantId) throws SQLException {
        String headSql = "SELECT o.order_id, o.user_id, u.username, o.subtotal, o.tax, o.total, o.status, "
                + "UNIX_TIMESTAMP(o.placed_at) AS placed_epoch, UNIX_TIMESTAMP(o.updated_at) AS updated_epoch "
                + "FROM orders o JOIN users u ON u.user_id = o.user_id "
                + "WHERE o.order_id = ? AND o.restaurant_id = ?";
        String lineSql = "SELECT item_name, unit_price, quantity FROM order_items "
                + "WHERE order_id = ? ORDER BY order_item_id";

        try (Connection c = DBConnection.getConnection()) {
            OrderSummary summary;
            java.math.BigDecimal subtotal;
            java.math.BigDecimal tax;
            try (PreparedStatement ps = c.prepareStatement(headSql)) {
                ps.setInt(1, orderId);
                ps.setInt(2, restaurantId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Optional.empty();
                    summary = new OrderSummary(rs.getInt("order_id"), rs.getInt("user_id"),
                            rs.getString("username"), rs.getBigDecimal("total"),
                            OrderStatus.valueOf(rs.getString("status")),
                            DbTime.read(rs, "placed_epoch"), DbTime.read(rs, "updated_epoch"));
                    subtotal = rs.getBigDecimal("subtotal");
                    tax = rs.getBigDecimal("tax");
                }
            }
            List<OrderLine> lines = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(lineSql)) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lines.add(new OrderLine(rs.getString("item_name"), rs.getBigDecimal("unit_price"),
                                rs.getInt("quantity")));
                    }
                }
            }
            return Optional.of(new OrderDetail(summary, subtotal, tax, lines));
        }
    }

    /**
     * Moves an order to a new status, only if it is currently in one of the "from" statuses.
     * Returns the number of rows changed (0 means someone already changed it).
     */
    public static int updateStatus(int orderId, int restaurantId, Collection<OrderStatus> from, OrderStatus to)
            throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ? AND restaurant_id = ? "
                + "AND status IN (" + placeholders(from.size()) + ")";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, to.name());
            ps.setInt(2, orderId);
            ps.setInt(3, restaurantId);
            int i = 4;
            for (OrderStatus st : from) ps.setString(i++, st.name());
            return ps.executeUpdate();
        }
    }

    private static String placeholders(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) sb.append(i == 0 ? "?" : ",?");
        return sb.toString();
    }

    private static List<OrderSummary> readSummaries(PreparedStatement ps) throws SQLException {
        List<OrderSummary> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new OrderSummary(rs.getInt("order_id"), rs.getInt("user_id"),
                        rs.getString("username"), rs.getBigDecimal("total"),
                        OrderStatus.valueOf(rs.getString("status")),
                        DbTime.read(rs, "placed_epoch"), DbTime.read(rs, "updated_epoch")));
            }
        }
        return list;
    }
}
