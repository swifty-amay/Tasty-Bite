package com.tastybite.ui;

import com.tastybite.model.MyOrder;
import com.tastybite.model.OrderLine;
import com.tastybite.model.OrderStatus;
import com.tastybite.service.UserOrderService;
import com.tastybite.util.Money;
import com.tastybite.util.Session;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Module F8: the customer's own orders, from every restaurant, newest first.
 * Orders that are still being handled ("In progress") come first, then "Past orders".
 * The screen refreshes itself every 10 seconds, so a customer sees the restaurant start
 * preparing or finish their order without doing anything.
 */
public class MyOrdersPanel extends JPanel {

    private static final Color LINE = new Color(0xEADFD3);
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int COLUMN_WIDTH = 720;
    private static final int POLL_MS = 10_000;

    private final MainFrame frame;
    private final JPanel body = new JPanel(new BorderLayout());
    private final JLabel updatedLabel = new JLabel(" ");
    private final Timer pollTimer = new Timer(POLL_MS, e -> reload(true));

    private JScrollPane scroll;
    private List<MyOrder> shown; // null until the first load
    private boolean loading;

    public MyOrdersPanel(MainFrame frame) {
        super(new BorderLayout());
        this.frame = frame;
        setBackground(UiTheme.PAGE_BG);
        body.setOpaque(false);

        add(buildHeader(), BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);

        showMessage("Loading your orders...", UiTheme.MUTED);
        reload(false);
    }

    /** Auto-refresh only runs while this screen is actually on display. */
    @Override
    public void addNotify() {
        super.addNotify();
        pollTimer.start();
    }

    @Override
    public void removeNotify() {
        pollTimer.stop();
        super.removeNotify();
    }

    // ---------------------------------------------------------------- header

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, UiTheme.PRIMARY),
                new EmptyBorder(8, 16, 8, 16)));

        JButton back = UiTheme.linkButton("\u2190 Menu");
        back.setFont(UiTheme.FONT_BOLD);
        back.addActionListener(e -> goToMenu());

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Tasty Bite");
        title.setFont(UiTheme.HEADING);
        title.setForeground(UiTheme.PRIMARY);
        title.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        JLabel sub = new JLabel("My Orders");
        sub.setFont(UiTheme.FONT);
        sub.setForeground(UiTheme.MUTED);
        sub.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        center.add(title);
        center.add(sub);

        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        spacer.setPreferredSize(back.getPreferredSize());

        header.add(back, BorderLayout.WEST);
        header.add(center, BorderLayout.CENTER);
        header.add(spacer, BorderLayout.EAST);
        return header;
    }

    /** Back to the menu the customer was browsing (or the restaurant list if none was chosen). */
    private void goToMenu() {
        if (Session.getRestaurant() != null) frame.showMenu();
        else frame.showRestaurantPicker();
    }

    // ---------------------------------------------------------------- loading

    /** @param silent true for the automatic refresh: no error messages, keep what is on screen */
    private void reload(boolean silent) {
        if (loading) return;
        loading = true;
        new SwingWorker<List<MyOrder>, Void>() {
            @Override
            protected List<MyOrder> doInBackground() throws Exception {
                return UserOrderService.getMyOrders();
            }

            @Override
            protected void done() {
                loading = false;
                try {
                    List<MyOrder> orders = get();
                    if (!orders.equals(shown)) { // only redraw when something changed
                        shown = orders;
                        buildContent(orders);
                    }
                    updatedLabel.setForeground(UiTheme.MUTED);
                    updatedLabel.setText("Updated " + LocalTime.now().format(CLOCK));
                } catch (Exception e) {
                    e.printStackTrace();
                    if (shown == null) {
                        showMessage("Could not load your orders. Check the database connection.", UiTheme.ERROR);
                    } else {
                        updatedLabel.setForeground(UiTheme.ERROR);
                        updatedLabel.setText("Could not refresh");
                    }
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

    /** Package-private so the layout can be checked without a database. */
    void buildContent(List<MyOrder> orders) {
        if (orders.isEmpty()) {
            showEmpty();
            return;
        }
        int scrollPos = scroll == null ? 0 : scroll.getVerticalScrollBar().getValue();

        List<MyOrder> active = new ArrayList<>();
        List<MyOrder> past = new ArrayList<>();
        for (MyOrder o : orders) (o.status().isActive() ? active : past).add(o);

        JPanel column = new JPanel() {
            @Override
            public Dimension getPreferredSize() { // fixed width, height from the content
                return new Dimension(COLUMN_WIDTH, super.getPreferredSize().height);
            }
        };
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));

        // refresh row
        updatedLabel.setFont(UiTheme.FONT_SMALL);
        updatedLabel.setForeground(UiTheme.MUTED);
        JButton refresh = UiTheme.linkButton("Refresh");
        refresh.addActionListener(e -> reload(false));
        JPanel refreshRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        refreshRow.setOpaque(false);
        refreshRow.add(updatedLabel);
        refreshRow.add(refresh);
        refreshRow.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        refreshRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        column.add(refreshRow);

        if (!active.isEmpty()) {
            column.add(sectionTitle("In progress", active.size()));
            for (MyOrder o : active) {
                column.add(Box.createVerticalStrut(10));
                column.add(buildCard(o));
            }
        }
        if (!past.isEmpty()) {
            if (!active.isEmpty()) column.add(Box.createVerticalStrut(18));
            column.add(sectionTitle("Past orders", past.size()));
            for (MyOrder o : past) {
                column.add(Box.createVerticalStrut(10));
                column.add(buildCard(o));
            }
        }

        VerticalPage page = new VerticalPage();
        page.setLayout(new GridBagLayout());
        page.setBackground(UiTheme.PAGE_BG);
        page.setBorder(new EmptyBorder(8, 16, 24, 16));
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
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(scrollPos));
    }

    private void showEmpty() {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        JLabel l = new JLabel("You have not placed any orders yet");
        l.setFont(UiTheme.HEADING);
        l.setForeground(UiTheme.MUTED);
        l.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        JButton browse = UiTheme.primaryButton("Browse the menu");
        browse.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        browse.addActionListener(e -> goToMenu());
        box.add(l);
        box.add(Box.createVerticalStrut(16));
        box.add(browse);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);
        center.add(box);
        scroll = null;
        body.removeAll();
        body.add(center, BorderLayout.CENTER);
        body.revalidate();
        body.repaint();
    }

    private static JPanel sectionTitle(String text, int count) {
        JLabel title = new JLabel(text);
        title.setFont(UiTheme.HEADING);
        title.setForeground(UiTheme.TEXT);
        JLabel n = new JLabel(String.valueOf(count));
        n.setFont(UiTheme.FONT);
        n.setForeground(UiTheme.MUTED);
        n.setBorder(new EmptyBorder(0, 8, 0, 0));
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setOpaque(false);
        p.add(title);
        p.add(n);
        p.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        return p;
    }

    /** One order as a card: restaurant, status, what is happening, the items and the total. */
    private JPanel buildCard(MyOrder o) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(14, 18, 14, 18)));
        card.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        // title row: Order #12 - Restaurant            [status]
        JLabel title = new JLabel("Order #" + o.orderId());
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(UiTheme.TEXT);
        JLabel where = new JLabel("\u00B7  " + o.restaurantName());
        where.setFont(UiTheme.FONT_BOLD);
        where.setForeground(UiTheme.PRIMARY);
        where.setBorder(new EmptyBorder(0, 8, 0, 0));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(title);
        left.add(where);
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(left, BorderLayout.WEST);
        titleRow.add(new StatusBadge(o.status()), BorderLayout.EAST);
        titleRow.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        card.add(titleRow);

        card.add(textLine("Placed " + OrderFormat.time(o.placedAt()), UiTheme.FONT, UiTheme.MUTED, 0));
        card.add(textLine(statusMessage(o), UiTheme.FONT_BOLD, statusColor(o.status()), 4));
        card.add(Box.createVerticalStrut(10));

        // items
        JPanel items = new JPanel();
        items.setOpaque(false);
        items.setLayout(new BoxLayout(items, BoxLayout.Y_AXIS));
        items.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, LINE));
        items.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        items.add(Box.createVerticalStrut(6));
        for (OrderLine line : o.lines()) {
            items.add(itemRow(line));
        }
        items.add(Box.createVerticalStrut(6));
        card.add(items);
        card.add(Box.createVerticalStrut(8));

        // total
        JLabel taxNote = new JLabel("incl. tax " + Money.format(o.tax()));
        taxNote.setFont(UiTheme.FONT_SMALL);
        taxNote.setForeground(UiTheme.MUTED);
        JLabel total = new JLabel("Total  " + Money.format(o.total()));
        total.setFont(new Font("SansSerif", Font.BOLD, 17));
        total.setForeground(UiTheme.PRIMARY);
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        totalRow.add(taxNote, BorderLayout.WEST);
        totalRow.add(total, BorderLayout.EAST);
        totalRow.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        totalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        card.add(totalRow);
        return card;
    }

    private static JPanel itemRow(OrderLine line) {
        JLabel name = new JLabel(line.quantity() + " \u00D7 " + line.name());
        name.setFont(UiTheme.FONT);
        name.setForeground(UiTheme.TEXT);
        JLabel price = new JLabel(Money.format(line.lineTotal()));
        price.setFont(UiTheme.FONT);
        price.setForeground(UiTheme.TEXT);
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(3, 0, 3, 0));
        row.add(name, BorderLayout.WEST);
        row.add(price, BorderLayout.EAST);
        row.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    private static JLabel textLine(String text, Font font, Color color, int topGap) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        l.setBorder(new EmptyBorder(topGap, 0, 0, 0));
        l.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        return l;
    }

    /** What is happening to the order, in plain words. */
    static String statusMessage(MyOrder o) {
        return switch (o.status()) {
            case PLACED -> "Waiting for " + o.restaurantName() + " to accept your order";
            case PREPARING -> o.restaurantName() + " is preparing your order";
            case COMPLETED -> "Completed " + OrderFormat.time(o.updatedAt());
            case CANCELLED -> "Cancelled by the restaurant " + OrderFormat.time(o.updatedAt());
        };
    }

    private static Color statusColor(OrderStatus s) {
        return switch (s) {
            case PLACED -> UiTheme.PRIMARY_HOVER;
            case PREPARING -> new Color(0x0D47A1);
            case COMPLETED -> new Color(0x1B5E20);
            case CANCELLED -> new Color(0xB71C1C);
        };
    }
}
