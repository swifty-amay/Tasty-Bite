package com.tastybite.ui;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import java.awt.Dimension;
import java.awt.Rectangle;

/** A vertical stack that always matches the scroll pane's width, so content inside can wrap or scroll sideways. */
public class VerticalPage extends JPanel implements Scrollable {

    @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }

    @Override public int getScrollableUnitIncrement(Rectangle r, int orientation, int direction) { return 20; }

    @Override public int getScrollableBlockIncrement(Rectangle r, int orientation, int direction) {
        return Math.max(r.height - 40, 20);
    }

    @Override public boolean getScrollableTracksViewportWidth() { return true; }

    @Override public boolean getScrollableTracksViewportHeight() { return false; }
}
