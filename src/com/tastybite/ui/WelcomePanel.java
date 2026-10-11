package com.tastybite.ui;

import com.tastybite.model.Role;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

/** Module F1, screen 1: title, Welcome, and the User / Manager buttons. */
public class WelcomePanel extends BackgroundPanel {

    public WelcomePanel(MainFrame frame) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("TASTY BITE");
        title.setFont(new Font("SansSerif", Font.BOLD, 64));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        JLabel welcome = new JLabel("WELCOME");
        welcome.setFont(new Font("SansSerif", Font.PLAIN, 28));
        welcome.setForeground(new Color(0xFFE8D6));
        welcome.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        JButton user = UiTheme.primaryButton("USER");
        JButton manager = UiTheme.primaryButton("MANAGER");
        user.setPreferredSize(new Dimension(170, 48));
        manager.setPreferredSize(new Dimension(170, 48));
        user.addActionListener(e -> frame.showLogin(Role.USER));
        manager.addActionListener(e -> frame.showLogin(Role.MANAGER));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 0));
        buttons.setOpaque(false);
        buttons.add(user);
        buttons.add(manager);
        buttons.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        box.add(title);
        box.add(Box.createVerticalStrut(6));
        box.add(welcome);
        box.add(Box.createVerticalStrut(36));
        box.add(buttons);
        add(box);
    }
}
