package com.tastybite.ui;

import com.tastybite.model.MenuItem;
import com.tastybite.model.MenuSection;
import com.tastybite.model.Restaurant;
import com.tastybite.service.MenuEditService;
import com.tastybite.service.ServiceException;
import com.tastybite.util.ImageLoader;
import com.tastybite.util.Money;
import com.tastybite.util.Session;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

/**
 * Module F7: Update Menu. One block per section (Snacks, Beverages...), each with an Add Item button
 * and the list of its items with Edit and Delete.
 */
public class MenuEditorPanel extends JPanel {

    private static final Color LINE = new Color(0xEADFD3);
    private static final int THUMB_W = 64;
    private static final int THUMB_H = 48;
    private static final int COLUMN_WIDTH = 760;

    private final MainFrame frame;
    private final Restaurant restaurant = Session.getRestaurant();
    private final JPanel body = new JPanel(new BorderLayout());
    private JScrollPane scroll;

    public MenuEditorPanel(MainFrame frame) {
        super(new BorderLayout());
        this.frame = frame;
        setBackground(UiTheme.PAGE_BG);
        body.setOpaque(false);

        JButton home = UiTheme.primaryButton("Home");
        home.addActionListener(e -> frame.showHome());
        add(new HeaderBar(frame, "Update Menu  \u00B7  " + restaurant.name(), home), BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

        showMessage("Loading menu...", UiTheme.MUTED);
        loadMenu();
    }

    // ---------------------------------------------------------------- loading

    private void loadMenu() {
        final int scrollPos = (scroll == null) ? 0 : scroll.getVerticalScrollBar().getValue();
        new SwingWorker<List<MenuSection>, Void>() {
            @Override
            protected List<MenuSection> doInBackground() throws Exception {
                List<MenuSection> menu = MenuEditService.getMenuForEditing();
                for (MenuSection s : menu) {
                    for (MenuItem item : s.items()) ImageLoader.load(item.imagePath(), THUMB_W, THUMB_H);
                }
                return menu;
            }

            @Override
            protected void done() {
                try {
                    buildContent(get());
                    SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(scrollPos));
                } catch (Exception e) {
                    showMessage("Could not load the menu. Check the database connection.", UiTheme.ERROR);
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void showMessage(String text, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(UiTheme.FONT);
        l.setForeground(color);
        scroll = null;
        body.removeAll();
        body.add(l, BorderLayout.CENTER);
        body.revalidate();
        body.repaint();
    }

    // ---------------------------------------------------------------- content

    /** Package-private so the layout can be checked without clicking. */
    void buildContent(List<MenuSection> menu) {
        JPanel column = new JPanel() {
            @Override
            public Dimension getPreferredSize() { // fixed width, height from the content
                return new Dimension(COLUMN_WIDTH, super.getPreferredSize().height);
            }
        };
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));

        JLabel hint = new JLabel("Customers see this menu when they choose " + restaurant.name() + ".");
        hint.setFont(UiTheme.FONT);
        hint.setForeground(UiTheme.MUTED);
        JButton addSection = UiTheme.primaryButton("Add Section");
        addSection.setBorder(new EmptyBorder(7, 18, 7, 18));
        addSection.addActionListener(e -> openSectionDialog(null));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(hint, BorderLayout.WEST);
        top.add(addSection, BorderLayout.EAST);
        top.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        top.setMaximumSize(new Dimension(Integer.MAX_VALUE, top.getPreferredSize().height));
        column.add(top);
        column.add(Box.createVerticalStrut(4));

        for (MenuSection section : menu) {
            column.add(Box.createVerticalStrut(14));
            column.add(buildSection(section));
        }

        VerticalPage page = new VerticalPage();
        page.setLayout(new GridBagLayout());
        page.setBackground(UiTheme.PAGE_BG);
        page.setBorder(new EmptyBorder(16, 16, 24, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.NORTH;
        gc.weightx = 1;
        gc.weighty = 1;
        page.add(column, gc);

        scroll = new JScrollPane(page);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UiTheme.PAGE_BG);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        body.removeAll();
        body.add(scroll, BorderLayout.CENTER);
        body.revalidate();
        body.repaint();
    }

    private JPanel buildSection(MenuSection section) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createLineBorder(LINE));
        card.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        // title + Add Item
        JLabel title = new JLabel(section.name());
        title.setFont(UiTheme.HEADING);
        title.setForeground(UiTheme.TEXT);
        JButton add = UiTheme.primaryButton("Add Item");
        add.setBorder(new EmptyBorder(7, 18, 7, 18));
        add.addActionListener(e -> openDialog("Add Item", section.name(), null, section.categoryId()));
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setBorder(new EmptyBorder(12, 18, 10, 18));
        JButton rename = UiTheme.linkButton("Rename");
        rename.addActionListener(e -> openSectionDialog(section));
        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleRow.setOpaque(false);
        titleRow.add(title);
        titleRow.add(rename);
        head.add(titleRow, BorderLayout.WEST);
        head.add(add, BorderLayout.EAST);
        head.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        head.setMaximumSize(new Dimension(Integer.MAX_VALUE, head.getPreferredSize().height));
        card.add(head);

        if (section.items().isEmpty()) {
            JLabel none = new JLabel("No items yet. Click Add Item to create the first one.");
            none.setFont(UiTheme.FONT);
            none.setForeground(UiTheme.MUTED);
            none.setBorder(new EmptyBorder(6, 18, 16, 18));
            none.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            card.add(none);
        } else {
            for (MenuItem item : section.items()) {
                card.add(buildRow(item, section.name()));
            }
        }
        return card;
    }

    private JPanel buildRow(MenuItem item, String sectionName) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, LINE), new EmptyBorder(8, 18, 8, 18)));
        row.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        row.setPreferredSize(new Dimension(10, 66));
        row.setMinimumSize(new Dimension(10, 66));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));

        JLabel name = new JLabel(item.name());
        name.setFont(UiTheme.FONT_BOLD);
        name.setForeground(UiTheme.TEXT);
        JLabel price = new JLabel(Money.format(item.price()));
        price.setFont(UiTheme.FONT);
        price.setForeground(UiTheme.PRIMARY);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(Box.createVerticalGlue());
        text.add(name);
        text.add(price);
        text.add(Box.createVerticalGlue());

        JButton edit = UiTheme.linkButton("Edit");
        JButton delete = UiTheme.linkButton("Delete");
        delete.setForeground(UiTheme.ERROR);
        edit.addActionListener(e -> openDialog("Edit Item", sectionName, item, item.categoryId()));
        delete.addActionListener(e -> confirmDelete(item));
        JPanel actions = new JPanel(new GridBagLayout());
        actions.setOpaque(false);
        actions.add(edit);
        actions.add(delete);

        row.add(new Thumb(item), BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        row.add(actions, BorderLayout.EAST);
        return row;
    }

    // ---------------------------------------------------------------- actions

    private void openDialog(String title, String sectionName, MenuItem existing, int categoryId) {
        ItemDialog dialog = new ItemDialog(SwingUtilities.getWindowAncestor(this), title, sectionName, existing,
                (name, price, image, removeImage) -> {
                    if (existing == null) {
                        MenuEditService.addItem(categoryId, name, price, image);
                    } else {
                        MenuEditService.updateItem(existing.id(), name, price, image, removeImage);
                    }
                });
        dialog.setVisible(true); // modal: continues when it closes
        if (dialog.wasSaved()) loadMenu();
    }

    /** existing == null creates a new section, otherwise renames that one. */
    private void openSectionDialog(MenuSection existing) {
        SectionDialog dialog = new SectionDialog(SwingUtilities.getWindowAncestor(this),
                existing == null ? "Add Section" : "Rename Section",
                existing == null ? null : existing.name(),
                name -> {
                    if (existing == null) MenuEditService.addSection(name);
                    else MenuEditService.renameSection(existing.categoryId(), name);
                });
        dialog.setVisible(true);
        if (dialog.wasSaved()) loadMenu();
    }

    private void confirmDelete(MenuItem item) {
        int choice = JOptionPane.showConfirmDialog(this,
                "Remove \"" + item.name() + "\" from the menu?\nOrders already placed are not affected.",
                "Delete item", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;
        try {
            MenuEditService.deleteItem(item.id());
        } catch (ServiceException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Could not delete", JOptionPane.WARNING_MESSAGE);
        }
        loadMenu();
    }

    // ---------------------------------------------------------------- thumbnail

    /** Small item photo, or a coloured placeholder with the first letter. */
    private static class Thumb extends JComponent {
        private final Image image;
        private final String name;

        Thumb(MenuItem item) {
            image = ImageLoader.load(item.imagePath(), THUMB_W, THUMB_H);
            name = item.name();
            Dimension d = new Dimension(THUMB_W, THUMB_H);
            setPreferredSize(d);
            setMinimumSize(d);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int y = (getHeight() - THUMB_H) / 2;
            g2.translate(0, y);
            g2.setClip(new RoundRectangle2D.Float(0, 0, THUMB_W, THUMB_H, 10, 10));
            if (image != null) {
                g2.drawImage(image, 0, 0, THUMB_W, THUMB_H, null);
            } else {
                float hue = Math.floorMod(name.hashCode(), 360) / 360f;
                g2.setColor(Color.getHSBColor(hue, 0.22f, 0.97f));
                g2.fillRect(0, 0, THUMB_W, THUMB_H);
                String letter = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
                g2.setFont(new Font("SansSerif", Font.BOLD, 24));
                g2.setColor(Color.getHSBColor(hue, 0.5f, 0.78f));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(letter, (THUMB_W - fm.stringWidth(letter)) / 2,
                        (THUMB_H - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }
}
