package com.tastybite.ui;

import com.tastybite.model.Role;
import com.tastybite.service.AuthException;
import com.tastybite.service.AuthService;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/**
 * Module F1, screen 3: registration form, shared by User and Manager.
 * Managers also enter their Restaurant Name.
 */
public class RegisterPanel extends BackgroundPanel {

    private final MainFrame frame;
    private final Role role;
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JPasswordField confirmField = new JPasswordField();
    private final JTextField restaurantField = new JTextField();
    private final JLabel errorLabel = UiTheme.errorLabel();

    public RegisterPanel(MainFrame frame, Role role) {
        this.frame = frame;
        this.role = role;

        UiTheme.Card card = new UiTheme.Card();
        card.setLayout(new BorderLayout(0, 16));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel avatar = new JLabel(new AvatarIcon(72));
        avatar.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        JLabel heading = new JLabel("Create " + role.label() + " Account");
        heading.setFont(UiTheme.HEADING);
        heading.setForeground(UiTheme.TEXT);
        heading.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        header.add(avatar);
        header.add(Box.createVerticalStrut(8));
        header.add(heading);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        UiTheme.addField(form, "Username", usernameField);
        UiTheme.addField(form, "Password", passwordField);
        UiTheme.addField(form, "Confirm Password", confirmField);
        if (role == Role.MANAGER) {
            UiTheme.addField(form, "Restaurant Name", restaurantField);
        }
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(8));
        JButton create = UiTheme.primaryButton("Create Account");
        create.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        create.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 42));
        form.add(create);

        JPanel links = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        links.setOpaque(false);
        JButton back = UiTheme.linkButton("Already have an account? Login");
        links.add(back);

        card.add(header, BorderLayout.NORTH);
        card.add(form, BorderLayout.CENTER);
        card.add(links, BorderLayout.SOUTH);
        add(card);

        create.addActionListener(e -> doRegister());
        back.addActionListener(e -> frame.showLogin(role));
    }

    private void doRegister() {
        errorLabel.setText(" ");
        try {
            AuthService.register(role,
                    usernameField.getText(),
                    new String(passwordField.getPassword()),
                    new String(confirmField.getPassword()),
                    restaurantField.getText());
        } catch (AuthException ex) {
            errorLabel.setText(ex.getMessage());
            return;
        }

        // success popup from the sketch, with a single Login button
        JOptionPane.showOptionDialog(this,
                "You have successfully created your account.",
                "Account created",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new Object[]{"Login"},
                "Login");
        frame.showLogin(role);
    }
}
