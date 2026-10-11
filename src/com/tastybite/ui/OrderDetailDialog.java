package com.tastybite.ui;

import com.tastybite.model.OrderDetail;
import com.tastybite.model.OrderLine;
import com.tastybite.model.OrderStatus;
import com.tastybite.model.OrderSummary;
import com.tastybite.service.ManagerOrderService;
import com.tastybite.service.ServiceException;
import com.tastybite.util.Money;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;

/**
 * "View Order" popup: who ordered, when, what, and the bill.
 * With showActions, a pending order can also be completed or cancelled from here.
 */
public class OrderDetailDialog extends JDialog {

    private static final Color LINE = new Color(0xEADFD3);
    private static final int[] LINE_WIDTHS = {0, 112, 96};

    private final OrderDetail detail;
    private final JLabel error = UiTheme.errorLabel();
    private boolean changed;

    public OrderDetailDialog(Window owner, OrderDetail detail, boolean showActions) {
        super(owner, "Order #" + detail.summary().orderId(), ModalityType.APPLICATION_MODAL);
        this.detail = detail;
        OrderSummary s = detail.summary();

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(22, 28, 20, 28));

        // title + status
        JLabel title = new JLabel("Order #" + s.orderId());
        title.setFont(UiTheme.HEADING);
        title.setForeground(UiTheme.TEXT);
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(new StatusBadge(s.status()), BorderLayout.EAST);
        titleRow.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        content.add(titleRow);
        content.add(Box.createVerticalStrut(10));

        content.add(infoLine("Customer", s.username()));
        content.add(infoLine("Placed", OrderFormat.time(s.placedAt())));
        switch (s.status()) {
            case PREPARING -> content.add(infoLine("Started preparing", OrderFormat.time(s.updatedAt())));
            case COMPLETED -> content.add(infoLine("Completed", OrderFormat.time(s.updatedAt())));
            case CANCELLED -> content.add(infoLine("Cancelled", OrderFormat.time(s.updatedAt())));
            default -> { }
        }
        content.add(Box.createVerticalStrut(14));

        // items
        JPanel items = new JPanel();
        items.setLayout(new BoxLayout(items, BoxLayout.Y_AXIS));
        items.setBackground(Color.WHITE);
        items.setBorder(BorderFactory.createLineBorder(LINE));
        items.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        boolean first = true;
        for (OrderLine line : detail.lines()) {
            JLabel name = new JLabel(line.name());
            name.setFont(UiTheme.FONT_BOLD);
            name.setForeground(UiTheme.TEXT);
            JLabel qty = new JLabel(line.quantity() + " \u00D7 " + Money.format(line.unitPrice()));
            qty.setFont(UiTheme.FONT);
            qty.setForeground(UiTheme.MUTED);
            JLabel total = new JLabel(Money.format(line.lineTotal()), SwingConstants.RIGHT);
            total.setFont(UiTheme.FONT_BOLD);
            total.setForeground(UiTheme.TEXT);
            items.add(RowLayout.row(LINE_WIDTHS, new JComponent[]{name, qty, total}, Color.WHITE, 38, !first));
            first = false;
        }
        content.add(items);
        content.add(Box.createVerticalStrut(12));

        // bill
        content.add(billRow("Subtotal", Money.format(detail.subtotal()), false));
        content.add(Box.createVerticalStrut(4));
        content.add(billRow("Tax", Money.format(detail.tax()), false));
        content.add(Box.createVerticalStrut(6));
        content.add(billRow("Total", Money.format(s.total()), true));
        content.add(Box.createVerticalStrut(14));
        content.add(error);
        content.add(Box.createVerticalStrut(6));

        // buttons
        JButton close = UiTheme.linkButton("Close");
        close.addActionListener(e -> dispose());
        if (showActions && s.status().isActive()) {
            JPanel row = new JPanel(new GridLayout(1, 0, 10, 0));
            row.setOpaque(false);
            if (s.status() == OrderStatus.PLACED) {
                JButton prepare = UiTheme.primaryButton("Start Preparing");
                prepare.addActionListener(e -> act(Action.PREPARE));
                row.add(prepare);
            }
            JButton complete = UiTheme.coloredButton("\u2713  Mark Complete", UiTheme.GREEN, UiTheme.GREEN_HOVER);
            JButton cancel = UiTheme.coloredButton("\u2717  Cancel Order", new Color(0xC62828), UiTheme.RED_HOVER);
            complete.addActionListener(e -> act(Action.COMPLETE));
            cancel.addActionListener(e -> act(Action.CANCEL));
            row.add(complete);
            row.add(cancel);
            row.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            content.add(row);
            content.add(Box.createVerticalStrut(4));
            close.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            content.add(close);
        } else {
            JButton done = UiTheme.primaryButton("Close");
            done.setAlignmentX(JComponent.LEFT_ALIGNMENT);
            done.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            done.addActionListener(e -> dispose());
            content.add(done);
            getRootPane().setDefaultButton(done);
        }

        setContentPane(content);
        setResizable(false);
        pack();
        setMinimumSize(new Dimension(560, getHeight()));
        setLocationRelativeTo(owner);
    }

    /** True if the order was completed or cancelled from this popup (the caller should refresh). */
    public boolean changed() { return changed; }

    private enum Action { PREPARE, COMPLETE, CANCEL }

    private void act(Action action) {
        int id = detail.summary().orderId();
        if (action == Action.CANCEL) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Cancel order #" + id + " from " + detail.summary().username() + "?\n"
                            + "It will stay in History as Cancelled.",
                    "Cancel order", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (choice != JOptionPane.YES_OPTION) return;
        }
        try {
            switch (action) {
                case PREPARE -> ManagerOrderService.startPreparing(id);
                case COMPLETE -> ManagerOrderService.markCompleted(id);
                case CANCEL -> ManagerOrderService.cancelOrder(id);
            }
            changed = true;
            dispose();
        } catch (ServiceException ex) {
            error.setText(ex.getMessage());
            changed = true; // the order was probably changed elsewhere, so ask the caller to refresh
        }
    }

    private static JPanel infoLine(String label, String value) {
        JLabel l = new JLabel(label + ":  ");
        l.setFont(UiTheme.FONT);
        l.setForeground(UiTheme.MUTED);
        JLabel v = new JLabel(value);
        v.setFont(UiTheme.FONT_BOLD);
        v.setForeground(UiTheme.TEXT);
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.CENTER);
        p.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        return p;
    }

    private static JPanel billRow(String label, String value, boolean bold) {
        Font f = bold ? new Font("SansSerif", Font.BOLD, 17) : UiTheme.FONT;
        JLabel l = new JLabel(label);
        l.setFont(f);
        l.setForeground(UiTheme.TEXT);
        JLabel v = new JLabel(value);
        v.setFont(f);
        v.setForeground(bold ? UiTheme.PRIMARY : UiTheme.TEXT);
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.EAST);
        p.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        return p;
    }
}
