package com.tastybite.dao;

import com.tastybite.model.Restaurant;
import com.tastybite.model.Role;
import com.tastybite.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/** Module B1: database access for registration and login. */
public final class AuthDAO {

    private AuthDAO() { }

    /** Row needed to check a login. */
    public record Credential(int id, String username, String hash) { }

    public static boolean usernameExists(Role role, String username) throws SQLException {
        String sql = "SELECT 1 FROM " + role.table() + " WHERE username = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static boolean restaurantNameExists(String name) throws SQLException {
        String sql = "SELECT 1 FROM restaurants WHERE name = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static void registerUser(String username, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.executeUpdate();
        }
    }

    /**
     * Creates the manager and their restaurant in one transaction.
     * The default Snacks and Beverages sections are created as part of the same transaction.
     */
    public static void registerManager(String username, String passwordHash, String restaurantName)
            throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int managerId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO managers (username, password_hash) VALUES (?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, username);
                    ps.setString(2, passwordHash);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Manager id was not generated");
                        managerId = keys.getInt(1);
                    }
                }
                int restaurantId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO restaurants (manager_id, name) VALUES (?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, managerId);
                    ps.setString(2, restaurantName);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Restaurant id was not generated");
                        restaurantId = keys.getInt(1);
                    }
                }
                // Default sections. The database trigger normally creates them already; IGNORE makes this
                // a harmless no-op then, and keeps it working on hosts that do not allow triggers.
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT IGNORE INTO categories (restaurant_id, name) VALUES (?, 'Snacks'), (?, 'Beverages')")) {
                    ps.setInt(1, restaurantId);
                    ps.setInt(2, restaurantId);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public static Optional<Credential> findCredential(Role role, String username) throws SQLException {
        String sql = "SELECT " + role.idColumn() + ", username, password_hash FROM "
                + role.table() + " WHERE username = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Credential(rs.getInt(1), rs.getString(2), rs.getString(3)));
                }
                return Optional.empty();
            }
        }
    }

    public static Optional<Restaurant> findRestaurantByManager(int managerId) throws SQLException {
        String sql = "SELECT restaurant_id, name FROM restaurants WHERE manager_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, managerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Restaurant(rs.getInt(1), rs.getString(2)));
                }
                return Optional.empty();
            }
        }
    }
}
