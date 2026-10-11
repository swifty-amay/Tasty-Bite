package com.tastybite.ui;

import com.tastybite.model.Role;
import com.tastybite.service.AuthException;
import com.tastybite.service.AuthService;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/** Module F1, screen 2: login form, shared by User and Manager. */
public class LoginPanel extends BackgroundPanel {

    private final MainFrame frame;
    private final Role role;
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JLabel errorLabel = UiTheme.errorLabel();

    public LoginPanel(MainFrame frame, Role role) {
        this.frame = frame;
        this.role = role;

        UiTheme.Card card = new UiTheme.Card();
        card.setLayout(new BorderLayout(0, 16));

        // header: avatar + heading
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel avatar = new JLabel(new AvatarIcon(72));
        avatar.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        JLabel heading = new JLabel(role.label() + " Login");
        heading.setFont(UiTheme.HEADING);
        heading.setForeground(UiTheme.TEXT);
        heading.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        header.add(avatar);
        header.add(Box.createVerticalStrut(8));
        header.add(heading);

        // form
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        UiTheme.addField(form, "Username", usernameField);
        UiTheme.addField(form, "Password", passwordField);
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(8));
        JButton loginBtn = UiTheme.primaryButton("Login");
        loginBtn.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 42));
        form.add(loginBtn);

        // footer links
        JPanel links = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        links.setOpaque(false);
        JButton register = UiTheme.linkButton("New " + role.label().toLowerCase() + "? Register");
        JButton back = UiTheme.linkButton("Back");
        links.add(register);
        links.add(back);

        card.add(header, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(links, BorderLayout.SOUTH);
        add(card);

        loginBtn.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());
        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());
        register.addActionListener(e -> frame.showRegister(role));
        back.addActionListener(e -> frame.showWelcome());
    }

    private void doLogin() {
        errorLabel.setText(" ");
        try {
            AuthService.login(role, usernameField.getText(), new String(passwordField.getPassword()));
            frame.showHome();
        } catch (AuthException ex) {
            errorLabel.setText(ex.getMessage());
            passwordField.setText("");
        }
    }
}
