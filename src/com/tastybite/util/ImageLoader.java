package com.tastybite.util;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads an image file, crops and scales it to exactly w x h (like CSS "cover"),
 * and caches the result. Returns null if there is no usable image,
 * in which case the card draws a placeholder.
 */
public final class ImageLoader {

    private static final Map<String, BufferedImage> CACHE = new ConcurrentHashMap<>();

    private ImageLoader() { }

    public static BufferedImage load(String path, int w, int h) {
        if (path == null || path.isBlank()) return null;
        String key = path + "|" + w + "x" + h;
        BufferedImage cached = CACHE.get(key);
        if (cached != null) return cached;

        try {
            File file = new File(path);
            if (!file.isFile()) return null;
            BufferedImage src = ImageIO.read(file);
            if (src == null) return null;

            double scale = Math.max((double) w / src.getWidth(), (double) h / src.getHeight());
            int dw = (int) Math.ceil(src.getWidth() * scale);
            int dh = (int) Math.ceil(src.getHeight() * scale);

            BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = out.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.drawImage(src, (w - dw) / 2, (h - dh) / 2, dw, dh, null);
            g.dispose();

            CACHE.put(key, out);
            return out;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }
}
