package com.tastybite.ui;

import com.tastybite.model.MenuItem;
import com.tastybite.model.MenuSection;
import com.tastybite.model.Restaurant;
import com.tastybite.service.MenuService;
import com.tastybite.util.Session;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseWheelListener;
import java.util.List;

/**
 * Module F2, screen 2: the menu. Header (profile, title, cart) on top, then a vertical scroll
 * page where every section (Snacks, Beverages...) is a horizontal scroll row of item cards.
 */
public class MenuPanel extends JPanel {

    private final MainFrame frame;
    private final Restaurant restaurant;
    private final JButton cartButton = UiTheme.primaryButton("Cart (0)");
    private final JPanel body = new JPanel(new BorderLayout());

    public MenuPanel(MainFrame frame) {
        super(new BorderLayout());
        this.frame = frame;
        this.restaurant = Session.getRestaurant();
        setBackground(UiTheme.PAGE_BG);

        body.setOpaque(false);
        add(buildHeader(), BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

        updateCartButton();
        showMessage("Loading menu...", UiTheme.MUTED);
        loadMenu();
    }

    // ---------------------------------------------------------------- header

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, UiTheme.PRIMARY),
                new EmptyBorder(8, 16, 8, 16)));

        // left: profile icon
        JButton profile = new JButton(new AvatarIcon(44));
        profile.setContentAreaFilled(false);
        profile.setBorderPainted(false);
        profile.setFocusPainted(false);
        profile.setToolTipText("Profile");
        profile.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        profile.addActionListener(e -> frame.showProfile());

        // center: title and restaurant
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Tasty Bite");
        title.setFont(UiTheme.HEADING);
        title.setForeground(UiTheme.PRIMARY);
        title.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        JButton change = UiTheme.linkButton(restaurant.name() + "  \u00B7  Change");
        change.setToolTipText("Choose a different restaurant");
        change.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        change.addActionListener(e -> changeRestaurant());
        center.add(title);
        center.add(change);

        // right: cart
        cartButton.addActionListener(e -> frame.showCart());

        header.add(profile, BorderLayout.WEST);
        header.add(center, BorderLayout.CENTER);
        JButton myOrders = UiTheme.linkButton("My Orders");
        myOrders.setFont(UiTheme.FONT_BOLD);
        myOrders.setToolTipText("See your past and current orders");
        myOrders.addActionListener(e -> frame.showMyOrders());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        right.add(myOrders);
        right.add(cartButton);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private void changeRestaurant() {
        if (!Session.getCart().isEmpty()) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Your cart has items from " + restaurant.name()
                            + ".\nChanging the restaurant will empty it. Continue?",
                    "Change restaurant", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) return;
            Session.getCart().clear();
        }
        frame.showRestaurantPicker();
    }

    private void updateCartButton() {
        cartButton.setText("Cart (" + Session.getCart().totalQuantity() + ")");
    }

    // ---------------------------------------------------------------- loading

    private void loadMenu() {
        new SwingWorker<List<MenuSection>, Void>() {
            @Override
            protected List<MenuSection> doInBackground() throws Exception {
                List<MenuSection> menu = MenuService.getMenu(restaurant.id());
                // warm the image cache here so building the cards on the UI thread is instant
                for (MenuSection s : menu) {
                    for (MenuItem item : s.items()) {
                        com.tastybite.util.ImageLoader.load(item.imagePath(), ItemCard.IMG_W, ItemCard.IMG_H);
                    }
                }
                return menu;
            }

            @Override
            protected void done() {
                try {
                    buildMenu(get());
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
        body.removeAll();
        body.add(l, BorderLayout.CENTER);
        body.revalidate();
        body.repaint();
    }

    // ---------------------------------------------------------------- menu content

    /** Package-private so it can be tested without a database. */
    void buildMenu(List<MenuSection> menu) {
        if (menu.isEmpty()) {
            showMessage("This restaurant has not added any items yet. Please check back later.", UiTheme.MUTED);
            return;
        }

        VerticalPage page = new VerticalPage();
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));
        page.setBackground(UiTheme.PAGE_BG);
        page.setBorder(new EmptyBorder(8, 0, 16, 0));

        JScrollPane outer = new JScrollPane(page);
        outer.setBorder(null);
        outer.getViewport().setBackground(UiTheme.PAGE_BG);
        outer.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        outer.getVerticalScrollBar().setUnitIncrement(20);

        for (MenuSection section : menu) {
            page.add(buildSection(section, outer));
        }

        body.removeAll();
        body.add(outer, BorderLayout.CENTER);
        body.revalidate();
        body.repaint();
    }

    private JPanel buildSection(MenuSection section, JScrollPane outer) {
        JLabel title = new JLabel(section.name());
        title.setFont(UiTheme.HEADING);
        title.setForeground(UiTheme.TEXT);
        title.setBorder(new EmptyBorder(14, 20, 2, 20));

        // the row of cards
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setBackground(UiTheme.PAGE_BG);
        row.setBorder(new EmptyBorder(8, 20, 8, 20));
        for (int i = 0; i < section.items().size(); i++) {
            if (i > 0) row.add(Box.createHorizontalStrut(14));
            row.add(new ItemCard(section.items().get(i), this::addToCart));
        }

        // horizontal scroll pane
        JScrollPane inner = new JScrollPane(row,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        inner.setBorder(null);
        inner.getViewport().setBackground(UiTheme.PAGE_BG);
        inner.getHorizontalScrollBar().setUnitIncrement(28);
        int rowHeight = ItemCard.HEIGHT + 16 + 18; // card + padding + scrollbar
        inner.setPreferredSize(new Dimension(10, rowHeight));
        inner.setMinimumSize(new Dimension(10, rowHeight));
        inner.setMaximumSize(new Dimension(Integer.MAX_VALUE, rowHeight));
        forwardWheelToOuter(inner, outer);

        JPanel sectionPanel = new JPanel(new BorderLayout());
        sectionPanel.setOpaque(false);
        sectionPanel.add(title, BorderLayout.NORTH);
        sectionPanel.add(inner, BorderLayout.CENTER);
        sectionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                sectionPanel.getPreferredSize().height + rowHeight));
        sectionPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        return sectionPanel;
    }

    /**
     * Mouse wheel over a horizontal row scrolls the whole page up and down
     * (otherwise the row would swallow it). Shift + wheel scrolls the row sideways.
     */
    private static void forwardWheelToOuter(JScrollPane inner, JScrollPane outer) {
        for (MouseWheelListener l : inner.getMouseWheelListeners()) {
            inner.removeMouseWheelListener(l);
        }
        inner.addMouseWheelListener(e -> {
            if (e.isShiftDown()) {
                JScrollBar h = inner.getHorizontalScrollBar();
                h.setValue(h.getValue() + e.getUnitsToScroll() * h.getUnitIncrement());
            } else {
                outer.dispatchEvent(SwingUtilities.convertMouseEvent(inner, e, outer));
            }
        });
    }

    private void addToCart(MenuItem item) {
        MenuService.addToCart(item);
        updateCartButton();
    }
}
