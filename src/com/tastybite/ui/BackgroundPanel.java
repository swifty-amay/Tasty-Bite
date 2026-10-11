package com.tastybite.ui;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.net.URL;

/**
 * Full-window background. Uses resources/images/background.jpg if present,
 * otherwise a warm gradient. Children are centered (GridBagLayout).
 */
public class BackgroundPanel extends JPanel {

    private final Image image;

    public BackgroundPanel() {
        super(new GridBagLayout());
        URL url = BackgroundPanel.class.getResource("/images/background.jpg");
        image = (url == null) ? null : new ImageIcon(url).getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        int w = getWidth();
        int h = getHeight();

        int iw = image == null ? -1 : image.getWidth(null);
        int ih = image == null ? -1 : image.getHeight(null);
        if (iw > 0 && ih > 0) {
            // scale to cover the whole window, keeping the aspect ratio
            double scale = Math.max((double) w / iw, (double) h / ih);
            int dw = (int) (iw * scale);
            int dh = (int) (ih * scale);
            g2.drawImage(image, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
        } else {
            g2.setPaint(new GradientPaint(0, 0, new Color(0xFF8A3D), w, h, new Color(0x7A1F00)));
            g2.fillRect(0, 0, w, h);
        }

        // dark overlay so white text stays readable on any image
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillRect(0, 0, w, h);
        g2.dispose();
    }
}
