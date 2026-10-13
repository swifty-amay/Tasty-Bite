package com.tastybite.dao;

import com.tastybite.model.Role;
import com.tastybite.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Module B3: reading and updating an account's own details. Works for users and managers. */
public final class ProfileDAO {

    private ProfileDAO() { }

    public record Row(String username, String hash) { }

    public static Optional<Row> findById(Role role, int id) throws SQLException {
        String sql = "SELECT username, password_hash FROM " + role.table()
                + " WHERE " + role.idColumn() + " = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(new Row(rs.getString(1), rs.getString(2))) : Optional.empty();
            }
        }
    }

    /** True if some OTHER account already uses this username. */
    public static boolean usernameTakenByOther(Role role, String username, int id) throws SQLException {
        String sql = "SELECT 1 FROM " + role.table()
                + " WHERE username = ? AND " + role.idColumn() + " <> ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setInt(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Updates the username, and the password hash too when newHash is not null. */
    public static void update(Role role, int id, String username, String newHash) throws SQLException {
        String sql = (newHash == null)
                ? "UPDATE " + role.table() + " SET username = ? WHERE " + role.idColumn() + " = ?"
                : "UPDATE " + role.table() + " SET username = ?, password_hash = ? WHERE " + role.idColumn() + " = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, username);
            if (newHash != null) ps.setString(i++, newHash);
            ps.setInt(i, id);
            ps.executeUpdate();
        }
    }
}
