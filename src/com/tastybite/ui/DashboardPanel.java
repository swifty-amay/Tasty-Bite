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
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Module F5: the manager's dashboard. Update Menu button, then the Latest Orders list
 * (pending orders, newest first) with View Order, Cancel (cross) and Mark Complete (tick).
 * The list refreshes itself every 10 seconds, so new orders appear without clicking anything.
 */
public class DashboardPanel extends JPanel {

    // S.No, Order, User (flexible), Placed, Total, Status, actions
    private static final int[] WIDTHS = {46, 70, 0, 140, 96, 124, 246};
    private static final Color LINE = new Color(0xEADFD3);
    private static final Color HEADER_BG = new Color(0xFBF3EA);
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int POLL_MS = 10_000;

    private final MainFrame frame;
    private final Restaurant restaurant = Session.getRestaurant();

    private final VerticalPage rowsPage = new VerticalPage();
    private final JScrollPane scroll = new JScrollPane(rowsPage);
    private final JLabel countLabel = new JLabel(" ");
    private final JLabel updatedLabel = new JLabel(" ");
    private final Timer pollTimer = new Timer(POLL_MS, e -> reload(true));

    private List<OrderSummary> shown; // what is on screen right now (null until the first load)
    private boolean loading;

    public DashboardPanel(MainFrame frame) {
        super(new BorderLayout());
        this.frame = frame;
        setBackground(UiTheme.PAGE_BG);

        JButton history = UiTheme.primaryButton("History");
        history.addActionListener(e -> frame.showHistory());
        add(new HeaderBar(frame, "Dashboard  \u00B7  " + restaurant.name(), history), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        showRowsMessage("Loading orders...", UiTheme.MUTED);
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

    // ---------------------------------------------------------------- layout

    private JPanel buildBody() {
        // "Update Menu: [Update]"
        JLabel menuTitle = new JLabel("Update Menu");
        menuTitle.setFont(UiTheme.HEADING);
        menuTitle.setForeground(UiTheme.TEXT);
        JLabel menuHint = new JLabel("Add, edit or remove items and sections on your menu");
        menuHint.setFont(UiTheme.FONT);
        menuHint.setForeground(UiTheme.MUTED);
        JPanel menuText = new JPanel();
        menuText.setOpaque(false);
        menuText.setLayout(new BoxLayout(menuText, BoxLayout.Y_AXIS));
        menuText.add(menuTitle);
        menuText.add(menuHint);
        JButton update = UiTheme.primaryButton("Update");
        update.addActionListener(e -> frame.showMenuEditor());
        JPanel menuCard = new JPanel(new BorderLayout());
        menuCard.setBackground(Color.WHITE);
        menuCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(14, 18, 14, 18)));
        menuCard.add(menuText, BorderLayout.WEST);
        menuCard.add(update, BorderLayout.EAST);

        // Latest Orders: title row
        JLabel ordersTitle = new JLabel("Latest Orders");
        ordersTitle.setFont(UiTheme.HEADING);
        ordersTitle.setForeground(UiTheme.TEXT);
        countLabel.setFont(UiTheme.FONT);
        countLabel.setForeground(UiTheme.MUTED);
        JPanel titleLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleLeft.setOpaque(false);
        titleLeft.add(ordersTitle);
        titleLeft.add(countLabel);

        updatedLabel.setFont(UiTheme.FONT_SMALL);
        updatedLabel.setForeground(UiTheme.MUTED);
        JButton refresh = UiTheme.linkButton("Refresh");
        refresh.addActionListener(e -> reload(false));
        JPanel titleRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        titleRight.setOpaque(false);
        titleRight.add(updatedLabel);
        titleRight.add(refresh);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setBorder(new EmptyBorder(12, 14, 8, 8));
        titleRow.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));
        titleRow.add(titleLeft, BorderLayout.WEST);
        titleRow.add(titleRight, BorderLayout.EAST);

        // column header
        JPanel header = RowLayout.row(WIDTHS, new JComponent[]{
                headerLabel("S.No", false), headerLabel("Order", false), headerLabel("User", false),
                headerLabel("Placed", false), headerLabel("Total", true), headerLabel("Status", false),
                headerLabel("", false)},
                HEADER_BG, 34, true);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(titleRow);
        top.add(header);

        rowsPage.setLayout(new BoxLayout(rowsPage, BoxLayout.Y_AXIS));
        rowsPage.setBackground(Color.WHITE);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(20);

        JPanel ordersCard = new JPanel(new BorderLayout());
        ordersCard.setBackground(Color.WHITE);
        ordersCard.setBorder(BorderFactory.createLineBorder(LINE));
        ordersCard.add(top, BorderLayout.NORTH);
        ordersCard.add(scroll, BorderLayout.CENTER);

        JPanel column = new JPanel(new BorderLayout(0, 14));
        column.setOpaque(false);
        column.setPreferredSize(new Dimension(940, 400));
        column.add(menuCard, BorderLayout.NORTH);
        column.add(ordersCard, BorderLayout.CENTER);

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

    // ---------------------------------------------------------------- loading and showing orders

    /** @param silent true for the automatic refresh: no error messages, just keep what is on screen */
    private void reload(boolean silent) {
        if (loading) return;
        loading = true;
        new SwingWorker<List<OrderSummary>, Void>() {
            @Override
            protected List<OrderSummary> doInBackground() throws Exception {
                return ManagerOrderService.getLatestOrders();
            }

            @Override
            protected void done() {
                loading = false;
                try {
                    List<OrderSummary> list = get();
                    if (!list.equals(shown)) { // only redraw when something actually changed
                        shown = list;
                        rebuildRows(list);
                    }
                    long pending = list.stream().filter(o -> o.status() == OrderStatus.PLACED).count();
                    long preparing = list.size() - pending;
                    countLabel.setText(list.isEmpty() ? " " : pending + " pending  \u00B7  " + preparing + " preparing");
                    updatedLabel.setForeground(UiTheme.MUTED);
                    updatedLabel.setText("Updated " + LocalTime.now().format(CLOCK));
                } catch (Exception e) {
                    e.printStackTrace();
                    if (shown == null) {
                        showRowsMessage("Could not load orders. Check the database connection.", UiTheme.ERROR);
                    } else {
                        updatedLabel.setForeground(UiTheme.ERROR);
                        updatedLabel.setText("Could not refresh");
                    }
                }
            }
        }.execute();
    }

    /** Package-private so the layout can be checked without a database. */
    void rebuildRows(List<OrderSummary> list) {
        int scrollPos = scroll.getVerticalScrollBar().getValue();
        rowsPage.removeAll();
        if (list.isEmpty()) {
            showRowsMessage("No new orders. New orders appear here automatically.", UiTheme.MUTED);
            return;
        }
        int n = 1;
        for (OrderSummary o : list) {
            rowsPage.add(buildRow(n++, o));
        }
        rowsPage.revalidate();
        rowsPage.repaint();
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(scrollPos));
    }

    private void showRowsMessage(String text, Color color) {
        rowsPage.removeAll();
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(UiTheme.FONT);
        l.setForeground(color);
        l.setBorder(new EmptyBorder(40, 0, 0, 0));
        l.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(l, BorderLayout.CENTER);
        wrap.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        rowsPage.add(wrap);
        rowsPage.revalidate();
        rowsPage.repaint();
    }

    private JPanel buildRow(int number, OrderSummary o) {
        JButton view = UiTheme.linkButton("View Order");
        view.addActionListener(e -> viewOrder(o));
        RoundIconButton cancel = new RoundIconButton(RoundIconButton.Kind.CROSS);
        RoundIconButton complete = new RoundIconButton(RoundIconButton.Kind.CHECK);
        cancel.addActionListener(e -> cancelOrder(o));
        complete.addActionListener(e -> completeOrder(o));

        // the start button only exists for pending orders; a gap keeps the other buttons lined up
        JComponent prepare;
        if (o.status() == OrderStatus.PLACED) {
            RoundIconButton start = new RoundIconButton(RoundIconButton.Kind.PREPARE);
            start.addActionListener(e -> prepareOrder(o));
            prepare = start;
        } else {
            JPanel gap = new JPanel();
            gap.setOpaque(false);
            gap.setPreferredSize(new Dimension(34, 34));
            prepare = gap;
        }

        JPanel actions = new JPanel(new GridBagLayout());
        actions.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new java.awt.Insets(0, 0, 0, 8);
        actions.add(view, gc);
        actions.add(prepare, gc);
        actions.add(cancel, gc);
        gc.insets = new java.awt.Insets(0, 0, 0, 0);
        actions.add(complete, gc);

        JPanel statusCell = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        statusCell.setOpaque(false);
        statusCell.add(new StatusBadge(o.status()));

        return RowLayout.row(WIDTHS, new JComponent[]{
                cell(String.valueOf(number), false, false),
                cell("#" + o.orderId(), true, false),
                cell(o.username(), true, false),
                cell(OrderFormat.timeShort(o.placedAt()), false, false),
                cell(Money.format(o.total()), true, true),
                statusCell,
                actions}, Color.WHITE, 60, true);
    }

    // ---------------------------------------------------------------- actions

    private void viewOrder(OrderSummary o) {
        new SwingWorker<OrderDetail, Void>() {
            @Override
            protected OrderDetail doInBackground() throws Exception {
                return ManagerOrderService.getDetail(o.orderId());
            }

            @Override
            protected void done() {
                try {
                    OrderDetailDialog dialog = new OrderDetailDialog(window(), get(), true);
                    dialog.setVisible(true);
                    if (dialog.changed()) reload(false);
                } catch (ExecutionException ex) {
                    warn(ex);
                    reload(false);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void prepareOrder(OrderSummary o) {
        runChange(() -> ManagerOrderService.startPreparing(o.orderId()));
    }

    private void completeOrder(OrderSummary o) {
        runChange(() -> ManagerOrderService.markCompleted(o.orderId()));
    }

    private void cancelOrder(OrderSummary o) {
        int choice = JOptionPane.showConfirmDialog(this,
                "Cancel order #" + o.orderId() + " from " + o.username() + "?\n"
                        + "It will stay in History as Cancelled.",
                "Cancel order", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            runChange(() -> ManagerOrderService.cancelOrder(o.orderId()));
        }
    }

    private interface Change {
        void run() throws ServiceException;
    }

    private void runChange(Change change) {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                change.run();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (ExecutionException ex) {
                    warn(ex);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                reload(false); // either way, show the current truth
            }
        }.execute();
    }

    private void warn(ExecutionException ex) {
        Throwable cause = ex.getCause();
        String message = (cause instanceof ServiceException)
                ? cause.getMessage() : "Something went wrong. Please try again.";
        JOptionPane.showMessageDialog(this, message, "Could not complete that", JOptionPane.WARNING_MESSAGE);
    }

    private Window window() {
        return SwingUtilities.getWindowAncestor(this);
    }
}
