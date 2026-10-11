package com.tastybite.ui;

import javax.swing.JButton;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** The round tick (Mark Complete, green) and cross (Cancel Order, red) buttons from the sketch. */
public class RoundIconButton extends JButton {

    public enum Kind { CHECK, CROSS }

    private static final Color RED = new Color(0xC62828);

    private final Kind kind;

    public RoundIconButton(Kind kind) {
        this.kind = kind;
        Dimension d = new Dimension(34, 34);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setToolTipText(kind == Kind.CHECK ? "Mark complete" : "Cancel order");
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        int cx = w / 2;
        int cy = h / 2;
        boolean pressed = getModel().isPressed();
        g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        if (kind == Kind.CHECK) {
            g2.setColor(pressed ? UiTheme.GREEN_HOVER : UiTheme.GREEN);
            g2.fillOval(0, 0, w - 1, h - 1);
            g2.setColor(Color.WHITE);
            g2.drawLine(cx - 7, cy, cx - 2, cy + 5);
            g2.drawLine(cx - 2, cy + 5, cx + 7, cy - 5);
        } else {
            g2.setColor(pressed ? new Color(0xFADBD8) : Color.WHITE);
            g2.fillOval(0, 0, w - 1, h - 1);
            g2.setColor(RED);
            g2.drawOval(1, 1, w - 3, h - 3);
            g2.drawLine(cx - 5, cy - 5, cx + 5, cy + 5);
            g2.drawLine(cx - 5, cy + 5, cx + 5, cy - 5);
        }
        g2.dispose();
    }
}
