package com.tastybite.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Shared colors, fonts and small component helpers so every screen looks the same. */
public final class UiTheme {

    public static final Color PRIMARY = new Color(0xE8590C);
    public static final Color PRIMARY_HOVER = new Color(0xC2410C);
    public static final Color TEXT = new Color(0x2B2B2B);
    public static final Color MUTED = new Color(0x6B6B6B);
    public static final Color ERROR = new Color(0xC62828);
    public static final Color PAGE_BG = new Color(0xFFF8F1);

    public static final Font FONT = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font HEADING = new Font("SansSerif", Font.BOLD, 22);

    private UiTheme() { }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_BOLD);
        b.setForeground(Color.WHITE);
        b.setBackground(PRIMARY);
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 24, 10, 24));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(PRIMARY_HOVER); }
            @Override public void mouseExited(MouseEvent e) { b.setBackground(PRIMARY); }
        });
        return b;
    }

    /** A text-only button, used for links like "New user? Register". */
    public static JButton linkButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT);
        b.setForeground(PRIMARY);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static void styleField(JTextField f) {
        f.setFont(FONT);
        f.setForeground(TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xBDBDBD)),
                new EmptyBorder(6, 8, 6, 8)));
        f.setPreferredSize(new Dimension(300, 36));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        f.setAlignmentX(JComponent.LEFT_ALIGNMENT);
    }

    /** Adds "Label" above a styled field to a vertical (BoxLayout) form panel. */
    public static void addField(JPanel form, String label, JTextField field) {
        JLabel l = new JLabel(label);
        l.setFont(FONT_BOLD);
        l.setForeground(TEXT);
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        styleField(field);
        form.add(l);
        form.add(javax.swing.Box.createVerticalStrut(4));
        form.add(field);
        form.add(javax.swing.Box.createVerticalStrut(12));
    }

    public static JLabel errorLabel() {
        JLabel l = new JLabel(" "); // a space keeps the height reserved
        l.setFont(FONT_SMALL);
        l.setForeground(ERROR);
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        return l;
    }

    /** White, rounded, slightly see-through panel that sits on top of the background image. */
    public static class Card extends JPanel {
        public Card() {
            setOpaque(false);
            setBorder(new EmptyBorder(28, 36, 24, 36));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 238));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
