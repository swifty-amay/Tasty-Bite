package com.tastybite.dao;

import com.tastybite.model.OrderDetail;
import com.tastybite.model.OrderLine;
import com.tastybite.model.OrderStatus;
import com.tastybite.model.OrderSummary;
import com.tastybite.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Modules B5 and B6: reading and updating a restaurant's orders.
 * Every statement is limited to one restaurant_id, so a manager only ever sees their own orders.
 */
public final class OrderQueryDAO {

    private static final String SUMMARY_SELECT =
            "SELECT o.order_id, o.user_id, u.username, o.total, o.status, o.placed_at, o.updated_at "
                    + "FROM orders o JOIN users u ON u.user_id = o.user_id "
                    + "WHERE o.restaurant_id = ? ";

    private OrderQueryDAO() { }

    /** Orders in one status, newest first (the dashboard uses PLACED). */
    public static List<OrderSummary> findByStatus(int restaurantId, OrderStatus status) throws SQLException {
        String sql = SUMMARY_SELECT + "AND o.status = ? ORDER BY o.placed_at DESC, o.order_id DESC";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            ps.setString(2, status.name());
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
                + "o.placed_at, o.updated_at FROM orders o JOIN users u ON u.user_id = o.user_id "
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
                            toLocal(rs.getTimestamp("placed_at")), toLocal(rs.getTimestamp("updated_at")));
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
     * Moves an order from one status to another, only if it is still in the "from" status.
     * Returns the number of rows changed (0 means someone already changed it).
     */
    public static int updateStatus(int orderId, int restaurantId, OrderStatus from, OrderStatus to)
            throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ? AND restaurant_id = ? AND status = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, to.name());
            ps.setInt(2, orderId);
            ps.setInt(3, restaurantId);
            ps.setString(4, from.name());
            return ps.executeUpdate();
        }
    }

    private static List<OrderSummary> readSummaries(PreparedStatement ps) throws SQLException {
        List<OrderSummary> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new OrderSummary(rs.getInt("order_id"), rs.getInt("user_id"),
                        rs.getString("username"), rs.getBigDecimal("total"),
                        OrderStatus.valueOf(rs.getString("status")),
                        toLocal(rs.getTimestamp("placed_at")), toLocal(rs.getTimestamp("updated_at"))));
            }
        }
        return list;
    }

    private static LocalDateTime toLocal(Timestamp t) {
        return t == null ? null : t.toLocalDateTime();
    }
}
