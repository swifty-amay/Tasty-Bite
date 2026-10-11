package com.tastybite.ui;

import com.tastybite.model.Bill;
import com.tastybite.model.Cart;
import com.tastybite.model.CartItem;
import com.tastybite.model.Restaurant;
import com.tastybite.service.OrderService;
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
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.concurrent.ExecutionException;

/**
 * Module F4: the cart. Ordered items with minus/plus buttons, the bill (subtotal, tax, total)
 * and the Place Order button.
 */
public class CartPanel extends JPanel {

    private static final Color LINE = new Color(0xEADFD3);

    private final MainFrame frame;
    private final Restaurant restaurant = Session.getRestaurant();

    private final VerticalPage rows = new VerticalPage();
    private final JScrollPane rowScroll = new JScrollPane(rows);
    private final CardLayout centerCards = new CardLayout();
    private final JPanel centerPanel = new JPanel(centerCards);

    private final JLabel subtotalValue = new JLabel();
    private final JLabel taxValue = new JLabel();
    private final JLabel totalValue = new JLabel();
    private final JButton placeBtn = UiTheme.primaryButton("Place Order");

    public CartPanel(MainFrame frame) {
        super(new BorderLayout());
        this.frame = frame;
        setBackground(UiTheme.PAGE_BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        refresh();
    }

    // ---------------------------------------------------------------- layout

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, UiTheme.PRIMARY),
                new EmptyBorder(8, 16, 8, 16)));

        JButton back = UiTheme.linkButton("\u2190 Menu");
        back.setFont(UiTheme.FONT_BOLD);
        back.addActionListener(e -> frame.showMenu());

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Tasty Bite");
        title.setFont(UiTheme.HEADING);
        title.setForeground(UiTheme.PRIMARY);
        title.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        JLabel sub = new JLabel("Your cart  \u00B7  " + restaurant.name());
        sub.setFont(UiTheme.FONT);
        sub.setForeground(UiTheme.MUTED);
        sub.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        center.add(title);
        center.add(sub);

        // invisible spacer the same width as the back button keeps the title centred
        JPanel spacer = new JPanel();
        spacer.setOpaque(false);
        spacer.setPreferredSize(back.getPreferredSize());

        header.add(back, BorderLayout.WEST);
        header.add(center, BorderLayout.CENTER);
        header.add(spacer, BorderLayout.EAST);
        return header;
    }

    private JPanel buildBody() {
        // items list
        rows.setLayout(new BoxLayout(rows, BoxLayout.Y_AXIS));
        rows.setBackground(Color.WHITE);
        rowScroll.setBorder(null);
        rowScroll.getViewport().setBackground(Color.WHITE);
        rowScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        rowScroll.getVerticalScrollBar().setUnitIncrement(20);

        JPanel itemsCard = new JPanel(new BorderLayout());
        itemsCard.setBorder(BorderFactory.createLineBorder(LINE));
        itemsCard.add(rowScroll, BorderLayout.CENTER);

        // empty state
        JPanel empty = new JPanel(new GridBagLayout());
        empty.setBackground(Color.WHITE);
        empty.setBorder(BorderFactory.createLineBorder(LINE));
        JPanel emptyBox = new JPanel();
        emptyBox.setOpaque(false);
        emptyBox.setLayout(new BoxLayout(emptyBox, BoxLayout.Y_AXIS));
        JLabel emptyLabel = new JLabel("Your cart is empty", SwingConstants.CENTER);
        emptyLabel.setFont(UiTheme.HEADING);
        emptyLabel.setForeground(UiTheme.MUTED);
        emptyLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        JButton browse = UiTheme.primaryButton("Browse the menu");
        browse.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        browse.addActionListener(e -> frame.showMenu());
        emptyBox.add(emptyLabel);
        emptyBox.add(Box.createVerticalStrut(16));
        emptyBox.add(browse);
        empty.add(emptyBox);

        centerPanel.setOpaque(false);
        centerPanel.add(itemsCard, "items");
        centerPanel.add(empty, "empty");

        // heading above the list
        JLabel heading = new JLabel("Ordered Items");
        heading.setFont(UiTheme.HEADING);
        heading.setForeground(UiTheme.TEXT);

        // the column holding heading, list and bill
        JPanel column = new JPanel(new BorderLayout(0, 12));
        column.setOpaque(false);
        column.setPreferredSize(new Dimension(720, 400));
        column.add(heading, BorderLayout.NORTH);
        column.add(centerPanel, BorderLayout.CENTER);
        column.add(buildBill(), BorderLayout.SOUTH);

        JPanel holder = new JPanel(new GridBagLayout());
        holder.setOpaque(false);
        holder.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.VERTICAL; // fixed width, full height
        gc.weighty = 1;
        holder.add(column, gc);
        return holder;
    }

    private JPanel buildBill() {
        JPanel bill = new JPanel();
        bill.setBackground(Color.WHITE);
        bill.setLayout(new BoxLayout(bill, BoxLayout.Y_AXIS));
        bill.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(14, 18, 16, 18)));

        bill.add(billRow("Subtotal", subtotalValue, false));
        bill.add(Box.createVerticalStrut(6));
        bill.add(billRow("Tax (" + Bill.taxPercentLabel() + ")", taxValue, false));
        bill.add(Box.createVerticalStrut(8));
        bill.add(billRow("Total", totalValue, true));
        bill.add(Box.createVerticalStrut(14));

        placeBtn.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        placeBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        placeBtn.addActionListener(e -> placeOrder());
        bill.add(placeBtn);
        return bill;
    }

    private JPanel billRow(String label, JLabel value, boolean bold) {
        JLabel l = new JLabel(label);
        Font f = bold ? new Font("SansSerif", Font.BOLD, 18) : UiTheme.FONT;
        l.setFont(f);
        l.setForeground(UiTheme.TEXT);
        value.setFont(f);
        value.setForeground(bold ? UiTheme.PRIMARY : UiTheme.TEXT);

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.add(l, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        row.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        return row;
    }

    // ---------------------------------------------------------------- data to screen

    /** Redraws the rows and the bill from the session cart. */
    private void refresh() {
        Cart cart = Session.getCart();
        int scrollPos = rowScroll.getVerticalScrollBar().getValue();

        rows.removeAll();
        for (CartItem ci : cart.items()) {
            rows.add(buildRow(ci));
        }

        Bill bill = OrderService.currentBill();
        subtotalValue.setText(Money.format(bill.subtotal()));
        taxValue.setText(Money.format(bill.tax()));
        totalValue.setText(Money.format(bill.total()));

        placeBtn.setEnabled(!cart.isEmpty());
        centerCards.show(centerPanel, cart.isEmpty() ? "empty" : "items");

        rows.revalidate();
        rows.repaint();
        SwingUtilities.invokeLater(() -> rowScroll.getVerticalScrollBar().setValue(scrollPos));
    }

    private JPanel buildRow(CartItem ci) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, LINE), new EmptyBorder(8, 18, 8, 18)));
        row.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        row.setPreferredSize(new Dimension(10, 66));
        row.setMinimumSize(new Dimension(10, 66));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));

        // left: name and unit price
        JLabel name = new JLabel(ci.item().name());
        name.setFont(UiTheme.FONT_BOLD);
        name.setForeground(UiTheme.TEXT);
        JLabel unit = new JLabel(Money.format(ci.item().price()) + " each");
        unit.setFont(UiTheme.FONT_SMALL);
        unit.setForeground(UiTheme.MUTED);
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(Box.createVerticalGlue());
        left.add(name);
        left.add(unit);
        left.add(Box.createVerticalGlue());

        // right: minus, quantity, plus, line total
        QtyButton minus = new QtyButton(false);
        QtyButton plus = new QtyButton(true);
        JLabel qty = new JLabel(String.valueOf(ci.quantity()), SwingConstants.CENTER);
        qty.setFont(UiTheme.FONT_BOLD);
        qty.setPreferredSize(new Dimension(32, 24));
        JLabel line = new JLabel(Money.format(ci.lineTotal()), SwingConstants.RIGHT);
        line.setFont(UiTheme.FONT_BOLD);
        line.setForeground(UiTheme.TEXT);
        line.setPreferredSize(new Dimension(96, 24));

        minus.addActionListener(e -> {
            Session.getCart().decrement(ci.item().id());
            refresh();
        });
        plus.addActionListener(e -> {
            Session.getCart().add(ci.item());
            refresh();
        });

        JPanel right = new JPanel(new GridBagLayout());
        right.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(0, 4, 0, 4);
        right.add(minus, gc);
        right.add(qty, gc);
        right.add(plus, gc);
        gc.insets = new Insets(0, 16, 0, 0);
        right.add(line, gc);

        row.add(left, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    // ---------------------------------------------------------------- place order

    private void placeOrder() {
        placeBtn.setEnabled(false);
        placeBtn.setText("Placing order...");

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return OrderService.placeOrder();
            }

            @Override
            protected void done() {
                placeBtn.setText("Place Order");
                try {
                    int orderId = get();
                    // success popup from the sketch, with a single OK button, then back to the menu
                    JOptionPane.showOptionDialog(CartPanel.this,
                            "Your order has been placed successfully.\nOrder number: #" + orderId,
                            "Order placed",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            new Object[]{"OK"},
                            "OK");
                    frame.showMenu();
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    String message = (cause instanceof ServiceException)
                            ? cause.getMessage()
                            : "Something went wrong while placing your order.";
                    JOptionPane.showMessageDialog(CartPanel.this, message,
                            "Order not placed", JOptionPane.WARNING_MESSAGE);
                    refresh(); // the cart may have been updated
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }
}
