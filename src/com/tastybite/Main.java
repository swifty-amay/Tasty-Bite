package com.tastybite;

import com.tastybite.ui.MainFrame;
import com.tastybite.util.AppPaths;
import com.tastybite.util.DBConnection;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.sql.Connection;

/** Application entry point. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // fall back to the default look and feel
            }

            // Fail early with a clear message if MySQL is not reachable
            try (Connection ignored = DBConnection.getConnection()) {
                // connection ok
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Could not connect to the database.\n\n" + e.getMessage()
                                + "\n\nCheck that:\n"
                                + " - MySQL is running and tasty_bite.sql has been executed\n"
                                + " - the settings file has the right user and password:\n   "
                                + AppPaths.settingsFile().getAbsolutePath() + "\n"
                                + " - the MySQL Connector/J jar is in the lib folder",
                        "Database error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            new MainFrame().setVisible(true);
        });
    }
}
