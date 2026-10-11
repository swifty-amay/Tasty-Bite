package com.tastybite.ui;

import com.tastybite.model.Restaurant;
import com.tastybite.service.MenuService;
import com.tastybite.util.Session;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.BorderFactory;

/** Module F2, screen 1: the user picks a restaurant, then its menu is loaded. */
public class RestaurantPickerPanel extends BackgroundPanel {

    private final MainFrame frame;
    private final DefaultListModel<Restaurant> model = new DefaultListModel<>();
    private final JList<Restaurant> list = new JList<>(model);
    private final JLabel status = new JLabel("Loading restaurants...");
    private final JButton continueBtn = UiTheme.primaryButton("View Menu");

    public RestaurantPickerPanel(MainFrame frame) {
        this.frame = frame;

        UiTheme.Card card = new UiTheme.Card();
        card.setLayout(new BorderLayout(0, 14));

        // header
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel avatar = new JLabel(new AvatarIcon(56));
        avatar.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        JLabel hello = new JLabel("Hello, " + Session.getCurrent().username());
        hello.setFont(UiTheme.FONT);
        hello.setForeground(UiTheme.MUTED);
        hello.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        JLabel heading = new JLabel("Choose a restaurant");
        heading.setFont(UiTheme.HEADING);
        heading.setForeground(UiTheme.TEXT);
        heading.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        header.add(avatar);
        header.add(Box.createVerticalStrut(6));
        header.add(hello);
        header.add(Box.createVerticalStrut(2));
        header.add(heading);

        // list
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(44);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object value, int index,
                                                          boolean selected, boolean focus) {
                JLabel c = (JLabel) super.getListCellRendererComponent(l, value, index, selected, false);
                c.setText(((Restaurant) value).name());
                c.setFont(UiTheme.FONT_BOLD);
                c.setBorder(new EmptyBorder(0, 14, 0, 14));
                c.setForeground(selected ? Color.WHITE : UiTheme.TEXT);
                c.setBackground(selected ? UiTheme.PRIMARY : Color.WHITE);
                return c;
            }
        });
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xBDBDBD)));
        scroll.setPreferredSize(new Dimension(360, 230));

        status.setFont(UiTheme.FONT_SMALL);
        status.setForeground(UiTheme.MUTED);
        status.setHorizontalAlignment(JLabel.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setOpaque(false);
        center.add(scroll, BorderLayout.CENTER);
        center.add(status, BorderLayout.SOUTH);

        // footer
        continueBtn.setEnabled(false);
        JButton logout = UiTheme.linkButton("Logout");
        JPanel south = new JPanel(new BorderLayout(0, 6));
        south.setOpaque(false);
        south.add(continueBtn, BorderLayout.NORTH);
        JPanel logoutRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logoutRow.setOpaque(false);
        logoutRow.add(logout);
        south.add(logoutRow, BorderLayout.SOUTH);

        card.add(header, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);
        card.add(south, BorderLayout.SOUTH);
        add(card);

        continueBtn.addActionListener(e -> openSelected());
        logout.addActionListener(e -> {
            Session.logout();
            frame.showWelcome();
        });
        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && list.getSelectedValue() != null) openSelected();
            }
        });
        list.addListSelectionListener(e -> continueBtn.setEnabled(list.getSelectedValue() != null));

        loadRestaurants();
    }

    private void openSelected() {
        Restaurant r = list.getSelectedValue();
        if (r == null) return;
        Session.setRestaurant(r);
        frame.showMenu();
    }

    private void loadRestaurants() {
        new SwingWorker<List<Restaurant>, Void>() {
            @Override
            protected List<Restaurant> doInBackground() throws Exception {
                return MenuService.getRestaurants();
            }

            @Override
            protected void done() {
                try {
                    List<Restaurant> restaurants = get();
                    restaurants.forEach(model::addElement);
                    if (restaurants.isEmpty()) {
                        status.setText("No restaurants yet. A manager needs to register first.");
                    } else {
                        status.setText(restaurants.size() + (restaurants.size() == 1 ? " restaurant" : " restaurants"));
                        list.setSelectedIndex(0);
                    }
                } catch (Exception e) {
                    status.setForeground(UiTheme.ERROR);
                    status.setText("Could not load restaurants. Check the database connection.");
                    e.printStackTrace();
                }
            }
        }.execute();
    }
}
