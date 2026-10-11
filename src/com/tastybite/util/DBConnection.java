package com.tastybite.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Shared JDBC connection factory.
 * Settings are read from db.properties in the working directory
 * (the project root). Missing keys fall back to local MySQL defaults.
 */
public final class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/tasty_bite?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static final Properties PROPS = load();

    private DBConnection() { }

    private static Properties load() {
        Properties p = new Properties();
        File f = new File("db.properties");
        if (f.exists()) {
            try (InputStream in = new FileInputStream(f)) {
                p.load(in);
            } catch (IOException e) {
                System.err.println("Could not read db.properties: " + e.getMessage());
            }
        }
        return p;
    }

    /** Opens a new connection. Callers must close it (use try-with-resources). */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                PROPS.getProperty("db.url", DEFAULT_URL),
                PROPS.getProperty("db.user", "root"),
                PROPS.getProperty("db.password", ""));
    }
}
