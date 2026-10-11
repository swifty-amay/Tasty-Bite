package com.tastybite.ui;

import com.tastybite.model.OrderDetail;
import com.tastybite.model.OrderStatus;
import com.tastybite.model.OrderSummary;
import com.tastybite.model.Restaurant;
import com.tastybite.service.ManagerOrderService;
import com.tastybite.service.ServiceException;
import com.tastybite.util.Money;
import com.tastybite.util.Session;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Module F6: order history. Every order of the restaurant, newest first, with its status.
 * Filter buttons narrow the list to Pending, Completed or Cancelled.
 */
public class HistoryPanel extends JPanel {

    // Order, Customer (flexible), Placed, Total, Status, action
    private static final int[] WIDTHS = {84, 0, 176, 100, 130, 80};
    private static final Color LINE = new Color(0xEADFD3);
    private static final Color HEADER_BG = new Color(0xFBF3EA);

    private enum Filter {
        ALL("All", null),
        PLACED("Pending", OrderStatus.PLACED),
        COMPLETED("Completed", OrderStatus.COMPLETED),
        CANCELLED("Cancelled", OrderStatus.CANCELLED);

        final String label;
        final OrderStatus status;

        Filter(String label, OrderStatus status) {
            this.label = label;
            this.status = status;
        }
    }

    private final MainFrame frame;
    private final Restaurant restaurant = Session.getRestaurant();

    private final VerticalPage rowsPage = new VerticalPage();
    private final JScrollPane scroll = new JScrollPane(rowsPage);
    private final JLabel note = new JLabel(" ");
    private final List<FilterChip> chips = new ArrayList<>();

    private List<OrderSummary> all = List.of();
    private Filter filter = Filter.ALL;

    public HistoryPanel(MainFrame frame) {
        super(new BorderLayout());
        this.frame = frame;
        setBackground(UiTheme.PAGE_BG);

        JButton home = UiTheme.primaryButton("Home");
        home.addActionListener(e -> frame.showHome());
        add(new HeaderBar(frame, "Order History  \u00B7  " + restaurant.name(), home), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        showMessage("Loading order history...", UiTheme.MUTED);
        load();
    }

    // ---------------------------------------------------------------- layout

    private JPanel buildBody() {
        // filter chips + refresh
        JPanel chipRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        chipRow.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        for (Filter f : Filter.values()) {
            FilterChip chip = new FilterChip(f.label);
            chip.setSelected(f == filter);
            chip.addActionListener(e -> {
                filter = f;
                rebuildRows();
            });
            group.add(chip);
            chips.add(chip);
            chipRow.add(chip);
        }
        JButton refresh = UiTheme.linkButton("Refresh");
        refresh.addActionListener(e -> load());
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.setBorder(new EmptyBorder(12, 8, 8, 8));
        topRow.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        topRow.add(chipRow, BorderLayout.WEST);
        topRow.add(refresh, BorderLayout.EAST);

        JPanel header = RowLayout.row(WIDTHS, new JComponent[]{
                headerLabel("Order", false), headerLabel("Customer", false), headerLabel("Placed", false),
                headerLabel("Total", true), headerLabel("Status", false), headerLabel("", false)},
                HEADER_BG, 34, true);

        note.setFont(UiTheme.FONT_SMALL);
        note.setForeground(UiTheme.MUTED);
        note.setBorder(new EmptyBorder(8, 18, 8, 18));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new javax.swing.BoxLayout(top, javax.swing.BoxLayout.Y_AXIS));
        top.add(topRow);
        top.add(header);

        rowsPage.setLayout(new javax.swing.BoxLayout(rowsPage, javax.swing.BoxLayout.Y_AXIS));
        rowsPage.setBackground(Color.WHITE);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(LINE));
        card.add(top, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.add(note, BorderLayout.SOUTH);

        JPanel column = new JPanel(new BorderLayout());
        column.setOpaque(false);
        column.setPreferredSize(new Dimension(940, 400));
        column.add(card, BorderLayout.CENTER);

        JPanel holder = new JPanel(new GridBagLayout());
        holder.setOpaque(false);
        holder.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.VERTICAL;
        gc.weighty = 1;
        holder.add(column, gc);
        return holder;
    }

    private static JLabel headerLabel(String text, boolean right) {
        JLabel l = new JLabel(text, right ? SwingConstants.RIGHT : SwingConstants.LEFT);
        l.setFont(UiTheme.FONT_BOLD);
        l.setForeground(UiTheme.MUTED);
        return l;
    }

    private static JLabel cell(String text, boolean bold, boolean right) {
        JLabel l = new JLabel(text, right ? SwingConstants.RIGHT : SwingConstants.LEFT);
        l.setFont(bold ? UiTheme.FONT_BOLD : UiTheme.FONT);
        l.setForeground(UiTheme.TEXT);
        return l;
    }

    // ---------------------------------------------------------------- loading and showing

    private void load() {
        new SwingWorker<ManagerOrderService.HistoryResult, Void>() {
            @Override
            protected ManagerOrderService.HistoryResult doInBackground() throws Exception {
                return ManagerOrderService.getHistory();
            }

            @Override
            protected void done() {
                try {
                    ManagerOrderService.HistoryResult result = get();
                    all = result.orders();
                    note.setText(result.truncated()
                            ? "Showing the latest " + ManagerOrderService.MAX_HISTORY + " orders."
                            : (all.size() == 1 ? "1 order in total." : all.size() + " orders in total."));
                    updateChipCounts();
                    rebuildRows();
                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage("Could not load the order history. Check the database connection.", UiTheme.ERROR);
                }
            }
        }.execute();
    }

    private void updateChipCounts() {
        Filter[] filters = Filter.values();
        for (int i = 0; i < filters.length; i++) {
            Filter f = filters[i];
            long n = all.stream().filter(o -> f.status == null || o.status() == f.status).count();
            chips.get(i).setText(f.label + " (" + n + ")");
        }
    }

    /** Package-private so the layout can be checked without a database. */
    void showOrders(List<OrderSummary> orders) {
        all = orders;
        updateChipCounts();
        rebuildRows();
    }

    private void rebuildRows() {
        rowsPage.removeAll();
        int shownCount = 0;
        for (OrderSummary o : all) {
            if (filter.status != null && o.status() != filter.status) continue;
            rowsPage.add(buildRow(o));
            shownCount++;
        }
        if (shownCount == 0) {
            showMessage(all.isEmpty() ? "No orders yet." : "No " + filter.label.toLowerCase() + " orders.",
                    UiTheme.MUTED);
            return;
        }
        rowsPage.revalidate();
        rowsPage.repaint();
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));
    }

    private void showMessage(String text, Color color) {
        rowsPage.removeAll();
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(UiTheme.FONT);
        l.setForeground(color);
        l.setBorder(new EmptyBorder(40, 0, 0, 0));
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(l, BorderLayout.CENTER);
        wrap.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        rowsPage.add(wrap);
        rowsPage.revalidate();
        rowsPage.repaint();
    }

    private JPanel buildRow(OrderSummary o) {
        JButton view = UiTheme.linkButton("View");
        view.addActionListener(e -> viewOrder(o));
        JPanel statusCell = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        statusCell.setOpaque(false);
        statusCell.add(new StatusBadge(o.status()));

        return RowLayout.row(WIDTHS, new JComponent[]{
                cell("#" + o.orderId(), true, false),
                cell(o.username(), true, false),
                cell(OrderFormat.time(o.placedAt()), false, false),
                cell(Money.format(o.total()), true, true),
                statusCell,
                view}, Color.WHITE, 54, true);
    }

    private void viewOrder(OrderSummary o) {
        new SwingWorker<OrderDetail, Void>() {
            @Override
            protected OrderDetail doInBackground() throws Exception {
                return ManagerOrderService.getDetail(o.orderId());
            }

            @Override
            protected void done() {
                try {
                    new OrderDetailDialog(SwingUtilities.getWindowAncestor(HistoryPanel.this), get(), false)
                            .setVisible(true);
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    JOptionPane.showMessageDialog(HistoryPanel.this,
                            cause instanceof ServiceException ? cause.getMessage() : "Something went wrong.",
                            "Could not open order", JOptionPane.WARNING_MESSAGE);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    // ---------------------------------------------------------------- filter chip

    /** Rounded toggle button: orange when selected, white with an outline when not. */
    private static class FilterChip extends JToggleButton {
        FilterChip(String text) {
            super(text);
            setFont(UiTheme.FONT_BOLD);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setBorder(new EmptyBorder(6, 16, 6, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int arc = getHeight();
            if (isSelected()) {
                g2.setColor(UiTheme.PRIMARY);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            } else {
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
                g2.setColor(new Color(0xD8C9BA));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            }
            g2.dispose();
            setForeground(isSelected() ? Color.WHITE : UiTheme.TEXT);
            super.paintComponent(g);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width, 32);
        }
    }
}
