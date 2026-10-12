package com.tastybite.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Shared JDBC connection factory.
 * Settings are read from db.properties (see AppPaths for where that file is looked for).
 * Missing keys fall back to local MySQL defaults.
 */
public final class DBConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/tasty_bite?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static final Properties PROPS = load();

    private DBConnection() { }

    static {
        // makes sure the MySQL driver is registered even inside a packaged app
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {
            // some other JDBC driver is on the classpath, or the usual automatic loading will find it
        }
    }

    private static Properties load() {
        Properties p = new Properties();
        File f = AppPaths.settingsFile();
        if (!f.exists()) {
            createTemplate(f);
        }
        if (f.isFile()) {
            try (InputStream in = new FileInputStream(f)) {
                p.load(in);
            } catch (IOException e) {
                System.err.println("Could not read " + f + ": " + e.getMessage());
            }
        }
        return p;
    }

    /** First start of the packaged app: leave a ready-to-edit settings file in the TastyBite folder. */
    private static void createTemplate(File f) {
        String text = "# MySQL connection settings for Tasty Bite\n"
                + "# Put your MySQL password after db.password=, save the file, then open the app again.\n"
                + "db.url=" + DEFAULT_URL + "\n"
                + "db.user=root\n"
                + "db.password=\n";
        try {
            Files.writeString(f.toPath(), text);
        } catch (IOException e) {
            System.err.println("Could not create " + f + ": " + e.getMessage());
        }
    }

    /** Opens a new connection. Callers must close it (use try-with-resources). */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                PROPS.getProperty("db.url", DEFAULT_URL),
                PROPS.getProperty("db.user", "root"),
                PROPS.getProperty("db.password", ""));
    }
}
