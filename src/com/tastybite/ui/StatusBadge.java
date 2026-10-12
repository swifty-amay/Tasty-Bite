package com.tastybite.ui;

import com.tastybite.model.OrderStatus;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Small coloured pill: Pending, Completed or Cancelled. */
public class StatusBadge extends JLabel {

    private final Color background;

    public StatusBadge(OrderStatus status) {
        super(status.label(), SwingConstants.CENTER);
        setFont(new Font("SansSerif", Font.BOLD, 12));
        setBorder(new EmptyBorder(4, 12, 4, 12));
        setOpaque(false);
        switch (status) {
            case PREPARING -> { background = new Color(0xDCE9FB); setForeground(new Color(0x0D47A1)); }
            case COMPLETED -> { background = new Color(0xDDF3E0); setForeground(new Color(0x1B5E20)); }
            case CANCELLED -> { background = new Color(0xFADBD8); setForeground(new Color(0xB71C1C)); }
            default -> { background = new Color(0xFFE3D1); setForeground(UiTheme.PRIMARY_HOVER); }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(background);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }
}
