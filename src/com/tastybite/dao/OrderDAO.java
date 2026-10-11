package com.tastybite.dao;

import com.tastybite.model.Bill;
import com.tastybite.model.CartItem;
import com.tastybite.model.MenuItem;
import com.tastybite.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Module B4: saving orders. */
public final class OrderDAO {

    private OrderDAO() { }

    /** Current (not deleted) menu items of a restaurant, by id. Used to check the cart before ordering. */
    public static Map<Integer, MenuItem> findCurrentItems(int restaurantId, Collection<Integer> itemIds)
            throws SQLException {
        Map<Integer, MenuItem> result = new HashMap<>();
        if (itemIds.isEmpty()) return result;

        StringBuilder sql = new StringBuilder(
                "SELECT item_id, category_id, name, price, image_path FROM menu_items "
                        + "WHERE restaurant_id = ? AND is_deleted = FALSE AND item_id IN (");
        for (int i = 0; i < itemIds.size(); i++) sql.append(i == 0 ? "?" : ",?");
        sql.append(")");

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            ps.setInt(idx++, restaurantId);
            for (Integer id : itemIds) ps.setInt(idx++, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MenuItem m = new MenuItem(rs.getInt("item_id"), rs.getInt("category_id"),
                            rs.getString("name"), rs.getBigDecimal("price"), rs.getString("image_path"));
                    result.put(m.id(), m);
                }
            }
        }
        return result;
    }

    /**
     * Saves the order and all its lines in one transaction (all or nothing).
     * Names and prices are copied into order_items so history stays correct if the menu changes later.
     *
     * @return the new order id
     */
    public static int insertOrder(int userId, int restaurantId, Bill bill, List<CartItem> lines)
            throws SQLException {
        String orderSql = "INSERT INTO orders (user_id, restaurant_id, subtotal, tax, total, status) "
                + "VALUES (?, ?, ?, ?, ?, 'PLACED')";
        String lineSql = "INSERT INTO order_items (order_id, item_id, item_name, unit_price, quantity) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int orderId;
                try (PreparedStatement ps = c.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, restaurantId);
                    ps.setBigDecimal(3, bill.subtotal());
                    ps.setBigDecimal(4, bill.tax());
                    ps.setBigDecimal(5, bill.total());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Order id was not generated");
                        orderId = keys.getInt(1);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(lineSql)) {
                    for (CartItem line : lines) {
                        ps.setInt(1, orderId);
                        ps.setInt(2, line.item().id());
                        ps.setString(3, line.item().name());
                        ps.setBigDecimal(4, line.item().price());
                        ps.setInt(5, line.quantity());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                c.commit();
                return orderId;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }
}
