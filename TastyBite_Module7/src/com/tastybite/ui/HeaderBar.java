package com.tastybite.ui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

/**
 * The header from the manager sketches: profile icon on the left, "Tasty Bite" in the middle
 * (with an optional second line), and one button on the right (History, Home, ...).
 */
public class HeaderBar extends JPanel {

    private static final int SIDE_WIDTH = 170; // equal side widths keep the title centred

    public HeaderBar(MainFrame frame, String subtitle, JComponent rightComponent) {
        super(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, UiTheme.PRIMARY),
                new EmptyBorder(8, 16, 8, 16)));

        // left: profile icon
        JButton profile = new JButton(new AvatarIcon(44));
        profile.setContentAreaFilled(false);
        profile.setBorderPainted(false);
        profile.setFocusPainted(false);
        profile.setToolTipText("Profile");
        profile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        profile.addActionListener(e -> frame.showProfile());
        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(profile, BorderLayout.WEST);
        left.setPreferredSize(new Dimension(SIDE_WIDTH, 44));

        // center: title (+ subtitle)
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Tasty Bite");
        title.setFont(UiTheme.HEADING);
        title.setForeground(UiTheme.PRIMARY);
        title.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        center.add(title);
        if (subtitle != null) {
            JLabel sub = new JLabel(subtitle);
            sub.setFont(UiTheme.FONT);
            sub.setForeground(UiTheme.MUTED);
            sub.setAlignmentX(JComponent.CENTER_ALIGNMENT);
            center.add(sub);
        }

        // right: one button (or nothing)
        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        right.setPreferredSize(new Dimension(SIDE_WIDTH, 44));
        if (rightComponent != null) {
            GridBagConstraints gc = new GridBagConstraints();
            gc.weightx = 1;
            gc.anchor = GridBagConstraints.EAST;
            right.add(rightComponent, gc);
        }

        add(left, BorderLayout.WEST);
        add(center, BorderLayout.CENTER);
        add(right, BorderLayout.EAST);
    }
}
