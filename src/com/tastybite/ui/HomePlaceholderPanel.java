package com.tastybite.ui;

import com.tastybite.model.Account;
import com.tastybite.model.Role;
import com.tastybite.util.Session;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;

/**
 * Temporary screen shown after login so Module F1 can be tested on its own.
 * It will be replaced by the restaurant picker / menu (F2) for users
 * and the admin dashboard (F5) for managers.
 */
public class HomePlaceholderPanel extends BackgroundPanel {

    public HomePlaceholderPanel(MainFrame frame) {
        Account account = Session.getCurrent();

        UiTheme.Card card = new UiTheme.Card();
        card.setLayout(new BorderLayout(0, 16));

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        JLabel avatar = new JLabel(new AvatarIcon(72));
        avatar.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        JLabel hello = new JLabel("Logged in as " + account.username());
        hello.setFont(UiTheme.HEADING);
        hello.setForeground(UiTheme.TEXT);
        hello.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        JLabel info = new JLabel(account.role() == Role.MANAGER && account.restaurant() != null
                ? "Manager of " + account.restaurant().name()
                : account.role().label());
        info.setFont(UiTheme.FONT);
        info.setForeground(UiTheme.MUTED);
        info.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        JLabel note = new JLabel("Login works. The next module replaces this screen.");
        note.setFont(UiTheme.FONT_SMALL);
        note.setForeground(UiTheme.MUTED);
        note.setAlignmentX(JComponent.CENTER_ALIGNMENT);

        body.add(avatar);
        body.add(Box.createVerticalStrut(10));
        body.add(hello);
        body.add(Box.createVerticalStrut(4));
        body.add(info);
        body.add(Box.createVerticalStrut(14));
        body.add(note);

        JButton logout = UiTheme.primaryButton("Logout");
        logout.addActionListener(e -> {
            Session.logout();
            frame.showWelcome();
        });

        card.add(body, BorderLayout.CENTER);
        card.add(logout, BorderLayout.SOUTH);
        add(card);
    }
}
