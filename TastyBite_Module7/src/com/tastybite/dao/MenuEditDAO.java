package com.tastybite.dao;

import com.tastybite.model.MenuItem;
import com.tastybite.util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/**
 * Module B7: changing a restaurant's menu.
 * Every statement is limited to one restaurant_id, so a manager can only touch their own menu.
 */
public final class MenuEditDAO {

    private MenuEditDAO() { }

    public static boolean categoryBelongsTo(int categoryId, int restaurantId) throws SQLException {
        String sql = "SELECT 1 FROM categories WHERE category_id = ? AND restaurant_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            ps.setInt(2, restaurantId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** A live (not deleted) item of this restaurant, or empty. */
    public static Optional<MenuItem> findItem(int itemId, int restaurantId) throws SQLException {
        String sql = "SELECT item_id, category_id, name, price, image_path FROM menu_items "
                + "WHERE item_id = ? AND restaurant_id = ? AND is_deleted = FALSE";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setInt(2, restaurantId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new MenuItem(rs.getInt("item_id"), rs.getInt("category_id"),
                        rs.getString("name"), rs.getBigDecimal("price"), rs.getString("image_path")));
            }
        }
    }

    /** Is there already a live item with this name in the category? (excludeItemId = 0 when adding) */
    public static boolean itemNameExists(int restaurantId, int categoryId, String name, int excludeItemId)
            throws SQLException {
        String sql = "SELECT 1 FROM menu_items WHERE restaurant_id = ? AND category_id = ? "
                + "AND name = ? AND is_deleted = FALSE AND item_id <> ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            ps.setInt(2, categoryId);
            ps.setString(3, name);
            ps.setInt(4, excludeItemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static int insertItem(int restaurantId, int categoryId, String name, BigDecimal price,
                                 String imagePath) throws SQLException {
        String sql = "INSERT INTO menu_items (restaurant_id, category_id, name, price, image_path) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, restaurantId);
            ps.setInt(2, categoryId);
            ps.setString(3, name);
            ps.setBigDecimal(4, price);
            ps.setString(5, imagePath);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Item id was not generated");
                return keys.getInt(1);
            }
        }
    }

    public static void updateItem(int itemId, int restaurantId, String name, BigDecimal price,
                                  String imagePath) throws SQLException {
        String sql = "UPDATE menu_items SET name = ?, price = ?, image_path = ? "
                + "WHERE item_id = ? AND restaurant_id = ? AND is_deleted = FALSE";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setBigDecimal(2, price);
            ps.setString(3, imagePath);
            ps.setInt(4, itemId);
            ps.setInt(5, restaurantId);
            ps.executeUpdate();
        }
    }

    /** Soft delete: the row stays so old orders keep working. */
    public static void softDeleteItem(int itemId, int restaurantId) throws SQLException {
        String sql = "UPDATE menu_items SET is_deleted = TRUE WHERE item_id = ? AND restaurant_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setInt(2, restaurantId);
            ps.executeUpdate();
        }
    }
}
