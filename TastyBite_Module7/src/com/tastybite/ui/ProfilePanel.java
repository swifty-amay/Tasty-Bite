package com.tastybite.ui;

import com.tastybite.model.Account;
import com.tastybite.model.Role;
import com.tastybite.service.AuthException;
import com.tastybite.service.ProfileService;
import com.tastybite.util.Session;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.concurrent.ExecutionException;

/**
 * Module F3: profile, shared by users and managers.
 * View mode shows the details read-only. Change Details switches to edit mode,
 * and Save shows the "saved successfully" popup and returns to view mode.
 */
public class ProfilePanel extends BackgroundPanel {

    private static final String DOTS = "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022";
    private static final Color READ_ONLY_BG = new Color(0xF3F3F3);

    private final MainFrame frame;
    private final UiTheme.Card card = new UiTheme.Card();
    private final JLabel heading = new JLabel("Your Profile");
    private JPanel form;

    public ProfilePanel(MainFrame frame) {
        this.frame = frame;
        Account account = Session.getCurrent();

        card.setLayout(new BorderLayout(0, 16));

        // header
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel avatar = new JLabel(new AvatarIcon(72));
        avatar.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        heading.setFont(UiTheme.HEADING);
        heading.setForeground(UiTheme.TEXT);
        heading.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        JLabel role = new JLabel(account.role().label());
        role.setFont(UiTheme.FONT);
        role.setForeground(UiTheme.MUTED);
        role.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        header.add(avatar);
        header.add(Box.createVerticalStrut(8));
        header.add(heading);
        header.add(Box.createVerticalStrut(2));
        header.add(role);

        // footer links
        JPanel links = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        links.setOpaque(false);
        JButton back = UiTheme.linkButton("\u2190 Back");
        JButton logout = UiTheme.linkButton("Logout");
        links.add(back);
        links.add(logout);
        back.addActionListener(e -> goBack());
        logout.addActionListener(e -> {
            Session.logout();
            frame.showWelcome();
        });

        card.add(header, BorderLayout.NORTH);
        card.add(links, BorderLayout.SOUTH);
        add(card);

        showViewMode();
    }

    /** Back to wherever the person came from. */
    private void goBack() {
        Account account = Session.getCurrent();
        if (account.role() == Role.USER) {
            if (Session.getRestaurant() != null) frame.showMenu();
            else frame.showRestaurantPicker();
        } else {
            frame.showHome();
        }
    }

    private void setForm(JPanel newForm) {
        if (form != null) card.remove(form);
        form = newForm;
        card.add(form, BorderLayout.CENTER);
        card.revalidate();
        card.repaint();
    }

    private static JPanel newFormPanel() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    private static void readOnly(JTextField f) {
        f.setEditable(false);
        f.setFocusable(false);
        f.setBackground(READ_ONLY_BG);
    }

    // ---------------------------------------------------------------- view mode

    void showViewMode() {
        Account account = Session.getCurrent();
        heading.setText("Your Profile");
        heading.revalidate();

        JPanel p = newFormPanel();
        JTextField username = new JTextField(account.username());
        JTextField password = new JTextField(DOTS);
        UiTheme.addField(p, "Username", username);
        UiTheme.addField(p, "Password", password);
        readOnly(username);
        readOnly(password);

        if (account.role() == Role.MANAGER && account.restaurant() != null) {
            JTextField restaurant = new JTextField(account.restaurant().name());
            UiTheme.addField(p, "Restaurant", restaurant);
            readOnly(restaurant);
        }

        p.add(Box.createVerticalStrut(6));
        JButton change = UiTheme.primaryButton("Change Details");
        change.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        change.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        change.addActionListener(e -> showEditMode());
        p.add(change);

        setForm(p);
    }

    // ---------------------------------------------------------------- edit mode

    /** Package-private so the layout can be checked without clicking. */
    void showEditMode() {
        Account account = Session.getCurrent();
        heading.setText("Edit Profile");
        heading.revalidate();

        JTextField username = new JTextField(account.username());
        JPasswordField newPassword = new JPasswordField();
        JPasswordField confirm = new JPasswordField();
        JPasswordField current = new JPasswordField();
        JLabel error = UiTheme.errorLabel();

        JPanel p = newFormPanel();
        UiTheme.addField(p, "Username", username);
        UiTheme.addField(p, "New Password (leave blank to keep)", newPassword);
        UiTheme.addField(p, "Confirm New Password", confirm);
        UiTheme.addField(p, "Current Password (required to save)", current);
        p.add(error);
        p.add(Box.createVerticalStrut(6));

        JButton save = UiTheme.primaryButton("Save");
        save.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        save.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        JButton cancel = UiTheme.linkButton("Cancel");
        cancel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        cancel.addActionListener(e -> showViewMode());
        p.add(save);
        p.add(Box.createVerticalStrut(4));
        p.add(cancel);

        save.addActionListener(e -> save(username, newPassword, confirm, current, error, save));
        current.addActionListener(e -> save.doClick());

        setForm(p);
        username.requestFocusInWindow();
    }

    private void save(JTextField username, JPasswordField newPassword, JPasswordField confirm,
                      JPasswordField current, JLabel error, JButton saveButton) {
        error.setText(" ");
        String user = username.getText();
        String newPw = new String(newPassword.getPassword());
        String confirmPw = new String(confirm.getPassword());
        String currentPw = new String(current.getPassword());

        saveButton.setEnabled(false);
        new SwingWorker<ProfileService.Result, Void>() {
            @Override
            protected ProfileService.Result doInBackground() throws Exception {
                return ProfileService.update(user, currentPw, newPw, confirmPw);
            }

            @Override
            protected void done() {
                saveButton.setEnabled(true);
                try {
                    showSavedPopup(get());
                    showViewMode();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    error.setText(cause instanceof AuthException
                            ? cause.getMessage() : "Something went wrong. Please try again.");
                    current.setText("");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    /** The confirmation popup from the sketch, with an OK button. */
    private void showSavedPopup(ProfileService.Result result) {
        String what;
        if (result.usernameChanged() && result.passwordChanged()) {
            what = "Your new username and password are saved successfully.";
        } else if (result.passwordChanged()) {
            what = "Your new password is saved successfully.";
        } else {
            what = "Your new username is saved successfully.";
        }
        JOptionPane.showOptionDialog(this, what, "Profile updated",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                null, new Object[]{"OK"}, "OK");
    }
}
