package com.tastybite.dao;

import com.tastybite.model.MyOrder;
import com.tastybite.model.OrderLine;
import com.tastybite.model.OrderStatus;
import com.tastybite.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Module B8: a customer reading their own orders. Always limited to one user_id. */
public final class UserOrderDAO {

    private UserOrderDAO() { }

    /** The user's orders from every restaurant, newest first, with their lines. */
    public static List<MyOrder> findByUser(int userId, int limit) throws SQLException {
        String orderSql = "SELECT o.order_id, r.name AS restaurant_name, o.subtotal, o.tax, o.total, "
                + "o.status, o.placed_at, o.updated_at "
                + "FROM orders o JOIN restaurants r ON r.restaurant_id = o.restaurant_id "
                + "WHERE o.user_id = ? ORDER BY o.placed_at DESC, o.order_id DESC LIMIT ?";

        try (Connection c = DBConnection.getConnection()) {
            List<Object[]> heads = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            try (PreparedStatement ps = c.prepareStatement(orderSql)) {
                ps.setInt(1, userId);
                ps.setInt(2, limit);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        heads.add(new Object[]{
                                rs.getInt("order_id"), rs.getString("restaurant_name"),
                                rs.getBigDecimal("subtotal"), rs.getBigDecimal("tax"), rs.getBigDecimal("total"),
                                OrderStatus.valueOf(rs.getString("status")),
                                toLocal(rs.getTimestamp("placed_at")), toLocal(rs.getTimestamp("updated_at"))});
                        ids.add(rs.getInt("order_id"));
                    }
                }
            }
            if (heads.isEmpty()) return new ArrayList<>();

            // all lines of those orders in one query
            StringBuilder lineSql = new StringBuilder(
                    "SELECT order_id, item_name, unit_price, quantity FROM order_items WHERE order_id IN (");
            for (int i = 0; i < ids.size(); i++) lineSql.append(i == 0 ? "?" : ",?");
            lineSql.append(") ORDER BY order_item_id");

            Map<Integer, List<OrderLine>> linesByOrder = new HashMap<>();
            try (PreparedStatement ps = c.prepareStatement(lineSql.toString())) {
                for (int i = 0; i < ids.size(); i++) ps.setInt(i + 1, ids.get(i));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        linesByOrder.computeIfAbsent(rs.getInt("order_id"), k -> new ArrayList<>())
                                .add(new OrderLine(rs.getString("item_name"), rs.getBigDecimal("unit_price"),
                                        rs.getInt("quantity")));
                    }
                }
            }

            List<MyOrder> result = new ArrayList<>();
            for (Object[] h : heads) {
                int id = (Integer) h[0];
                result.add(new MyOrder(id, (String) h[1], (BigDecimal) h[2], (BigDecimal) h[3],
                        (BigDecimal) h[4], (OrderStatus) h[5], (LocalDateTime) h[6], (LocalDateTime) h[7],
                        linesByOrder.getOrDefault(id, List.of())));
            }
            return result;
        }
    }

    private static LocalDateTime toLocal(Timestamp t) {
        return t == null ? null : t.toLocalDateTime();
    }
}
