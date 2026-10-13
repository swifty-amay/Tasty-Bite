package com.tastybite.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
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

    // -------- connection pool --------
    // Opening a connection to a database on the internet takes a second or more (network, encryption,
    // login). So finished connections are kept for reuse instead of being closed.

    private static final int MAX_IDLE = 4;
    private static final long CHECK_AFTER_IDLE_MS = 2_000;
    private static final Deque<Idle> IDLE = new ArrayDeque<>();

    private record Idle(Connection connection, long since) { }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            synchronized (IDLE) {
                for (Idle idle : IDLE) closeQuietly(idle.connection());
                IDLE.clear();
            }
        }));
    }

    /**
     * Gives a connection. Callers must close it (use try-with-resources). Closing hands it back to the pool,
     * undoing any unfinished transaction first.
     */
    public static Connection getConnection() throws SQLException {
        Connection real = takeIdle();
        if (real == null) {
            real = DriverManager.getConnection(
                    PROPS.getProperty("db.url", DEFAULT_URL),
                    PROPS.getProperty("db.user", "root"),
                    PROPS.getProperty("db.password", ""));
        }
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class}, new PooledHandler(real));
    }

    private static Connection takeIdle() {
        while (true) {
            Idle idle;
            synchronized (IDLE) {
                idle = IDLE.pollFirst();
            }
            if (idle == null) return null;
            boolean longIdle = System.currentTimeMillis() - idle.since() > CHECK_AFTER_IDLE_MS;
            try {
                if (!longIdle || idle.connection().isValid(2)) return idle.connection();
            } catch (SQLException ignored) {
                // fall through: the connection is dead
            }
            closeQuietly(idle.connection());
        }
    }

    private static void giveBack(Connection real) {
        try {
            if (real.isClosed()) return;
            if (!real.getAutoCommit()) {
                try {
                    real.rollback();
                } catch (SQLException ignored) {
                    // the connection may be broken; the check below throws it away
                }
                real.setAutoCommit(true);
            }
            synchronized (IDLE) {
                if (IDLE.size() < MAX_IDLE) {
                    IDLE.addFirst(new Idle(real, System.currentTimeMillis()));
                    return;
                }
            }
            real.close();
        } catch (SQLException e) {
            closeQuietly(real);
        }
    }

    private static void closeQuietly(Connection c) {
        try {
            c.close();
        } catch (SQLException ignored) {
            // nothing more to do
        }
    }

    /** What the caller holds: behaves like the real connection, but close() returns it to the pool. */
    private static final class PooledHandler implements InvocationHandler {
        private final Connection real;
        private boolean released;

        PooledHandler(Connection real) { this.real = real; }

        @Override
        public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String name = method.getName();
            if (name.equals("close") && method.getParameterCount() == 0) {
                if (!released) {
                    released = true;
                    giveBack(real);
                }
                return null;
            }
            if (name.equals("isClosed") && method.getParameterCount() == 0) {
                return released || real.isClosed();
            }
            if (released) {
                throw new SQLException("This connection has already been closed");
            }
            try {
                return method.invoke(real, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }
    }
}
