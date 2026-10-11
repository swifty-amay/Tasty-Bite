package com.tastybite.ui;

import com.tastybite.model.Role;
import com.tastybite.util.Session;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Dimension;

/**
 * The single application window. Screens are swapped in and out by the show... methods,
 * which act as the navigator for every module.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("Tasty Bite");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 680);
        setMinimumSize(new Dimension(800, 560));
        setLocationRelativeTo(null);
        showWelcome();
    }

    private void show(JPanel panel) {
        setContentPane(panel);
        revalidate();
        repaint();
    }

    // ---- Module F1
    public void showWelcome() { show(new WelcomePanel(this)); }

    public void showLogin(Role role) { show(new LoginPanel(this, role)); }

    public void showRegister(Role role) { show(new RegisterPanel(this, role)); }

    /** Where to go after a successful login. */
    public void showHome() {
        if (Session.getCurrent().role() == Role.USER) {
            showRestaurantPicker();
        } else {
            show(new HomePlaceholderPanel(this)); // replaced by the admin dashboard in F5
        }
    }

    // ---- Module F2
    public void showRestaurantPicker() { show(new RestaurantPickerPanel(this)); }

    public void showMenu() { show(new MenuPanel(this)); }

    // ---- Not built yet: temporary messages until these modules exist
    public void showProfile() {
        JOptionPane.showMessageDialog(this, "The profile screen is built in Module F3.",
                "Coming next", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showCart() {
        int count = Session.getCart().totalQuantity();
        JOptionPane.showMessageDialog(this,
                "The cart screen is built in Module F4.\nItems in your cart so far: " + count,
                "Coming next", JOptionPane.INFORMATION_MESSAGE);
    }
}
