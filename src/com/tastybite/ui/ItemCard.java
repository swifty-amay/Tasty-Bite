package com.tastybite.ui;

import com.tastybite.model.MenuItem;
import com.tastybite.util.ImageLoader;
import com.tastybite.util.Money;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Consumer;

/** A menu item card: image, name, price and a round + button. */
public class ItemCard extends JPanel {

    public static final int IMG_W = 150;
    public static final int IMG_H = 110;
    public static final int WIDTH = 172;
    public static final int HEIGHT = 214;

    public ItemCard(MenuItem item, Consumer<MenuItem> onAdd) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xEADFD3)),
                new EmptyBorder(10, 10, 10, 10)));
        Dimension size = new Dimension(WIDTH, HEIGHT);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);

        ImageBox image = new ImageBox(item);
        image.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        JLabel name = new JLabel(item.name());
        name.setFont(UiTheme.FONT_BOLD);
        name.setForeground(UiTheme.TEXT);
        name.setToolTipText(item.name());
        name.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        name.setPreferredSize(new Dimension(IMG_W, 24));
        name.setMaximumSize(new Dimension(IMG_W, 24));

        JLabel price = new JLabel(Money.format(item.price()));
        price.setFont(new Font("SansSerif", Font.BOLD, 15));
        price.setForeground(UiTheme.PRIMARY);

        RoundAddButton add = new RoundAddButton();
        add.addActionListener(e -> {
            onAdd.accept(item);
            add.flash();
        });

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(price, BorderLayout.WEST);
        bottom.add(add, BorderLayout.EAST);
        bottom.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        bottom.setPreferredSize(new Dimension(IMG_W, 36));
        bottom.setMaximumSize(new Dimension(IMG_W, 36));

        add(image);
        add(Box.createVerticalStrut(8));
        add(name);
        add(Box.createVerticalGlue());
        add(bottom);
    }

    /** Item photo, or a coloured placeholder with the first letter if there is no image. */
    private static class ImageBox extends JComponent {
        private final Image image;
        private final String name;

        ImageBox(MenuItem item) {
            image = ImageLoader.load(item.imagePath(), IMG_W, IMG_H);
            name = item.name();
            Dimension d = new Dimension(IMG_W, IMG_H);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));

            if (image != null) {
                g2.drawImage(image, 0, 0, getWidth(), getHeight(), null);
            } else {
                float hue = Math.floorMod(name.hashCode(), 360) / 360f;
                g2.setColor(Color.getHSBColor(hue, 0.22f, 0.97f));
                g2.fillRect(0, 0, getWidth(), getHeight());

                String letter = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
                g2.setFont(new Font("SansSerif", Font.BOLD, 46));
                g2.setColor(Color.getHSBColor(hue, 0.5f, 0.78f));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(letter)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(letter, x, y);
            }
            g2.dispose();
        }
    }

    /** Round orange button with a white plus. After a click it shows a tick for a moment. */
    private static class RoundAddButton extends JButton {
        private boolean added;

        RoundAddButton() {
            Dimension d = new Dimension(34, 34);
            setPreferredSize(d);
            setMinimumSize(d);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setToolTipText("Add to cart");
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        void flash() {
            added = true;
            repaint();
            Timer t = new Timer(700, e -> {
                added = false;
                repaint();
            });
            t.setRepeats(false);
            t.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isPressed() ? UiTheme.PRIMARY_HOVER : UiTheme.PRIMARY);
            g2.fillOval(0, 0, getWidth(), getHeight());

            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            if (added) {
                g2.drawLine(cx - 7, cy, cx - 2, cy + 5);
                g2.drawLine(cx - 2, cy + 5, cx + 7, cy - 5);
            } else {
                g2.drawLine(cx - 7, cy, cx + 7, cy);
                g2.drawLine(cx, cy - 7, cx, cy + 7);
            }
            g2.dispose();
        }
    }
}
