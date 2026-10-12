package com.tastybite.ui;

import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Small round outlined button with a minus or plus, used for cart quantities. */
public class QtyButton extends JButton {

    private final boolean plus;

    public QtyButton(boolean plus) {
        this.plus = plus;
        Dimension d = new Dimension(30, 30);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setToolTipText(plus ? "Add one" : "Remove one");
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (getModel().isPressed()) {
            g2.setColor(new Color(0xFFE3D1));
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
        }
        g2.setColor(UiTheme.PRIMARY);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawOval(1, 1, getWidth() - 3, getHeight() - 3);
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        g2.drawLine(cx - 6, cy, cx + 6, cy);
        if (plus) g2.drawLine(cx, cy - 6, cx, cy + 6);
        g2.dispose();
    }
}
