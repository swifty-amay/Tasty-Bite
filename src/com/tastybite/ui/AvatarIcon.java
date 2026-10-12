package com.tastybite.ui;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

/** The round "profile" icon from the sketches, drawn in code so no image file is needed. */
public class AvatarIcon implements Icon {

    private final int size;

    public AvatarIcon(int size) { this.size = size; }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(UiTheme.PRIMARY);
        g2.setStroke(new BasicStroke(2.5f));
        g2.draw(new Ellipse2D.Float(x + 1, y + 1, size - 2, size - 2));

        g2.setClip(new Ellipse2D.Float(x + 1, y + 1, size - 2, size - 2));
        int head = size / 3;
        g2.fillOval(x + (size - head) / 2, y + size / 5, head, head);
        int body = (int) (size * 0.7);
        g2.fillOval(x + (size - body) / 2, y + (int) (size * 0.58), body, body);
        g2.dispose();
    }

    @Override public int getIconWidth() { return size; }

    @Override public int getIconHeight() { return size; }
}
