package com.tastybite.ui;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/** Builds one table row so the header and every data row share the same column widths. */
final class RowLayout {

    private static final Color LINE = new Color(0xEADFD3);

    private RowLayout() { }

    /**
     * @param widths column widths in pixels; 0 means "take the leftover space"
     * @param cells  one component per column
     */
    static JPanel row(int[] widths, JComponent[] cells, Color background, int height, boolean topLine) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(background);
        p.setBorder(BorderFactory.createCompoundBorder(
                topLine ? BorderFactory.createMatteBorder(1, 0, 0, 0, LINE) : BorderFactory.createEmptyBorder(),
                new EmptyBorder(0, 16, 0, 16)));

        for (int i = 0; i < cells.length; i++) {
            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx = i;
            gc.anchor = GridBagConstraints.WEST;
            gc.insets = new Insets(0, 0, 0, i == cells.length - 1 ? 0 : 8);
            if (widths[i] > 0) {
                Dimension d = new Dimension(widths[i], cells[i].getPreferredSize().height);
                cells[i].setPreferredSize(d);
                cells[i].setMinimumSize(d);
                gc.weightx = 0;
            } else {
                gc.weightx = 1;
                gc.fill = GridBagConstraints.HORIZONTAL;
            }
            p.add(cells[i], gc);
        }

        p.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        p.setPreferredSize(new Dimension(10, height));
        p.setMinimumSize(new Dimension(10, height));
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));
        return p;
    }
}
