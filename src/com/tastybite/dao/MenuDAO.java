package com.tastybite.dao;

import com.tastybite.model.MenuItem;
import com.tastybite.model.MenuSection;
import com.tastybite.model.Restaurant;
import com.tastybite.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Module B2: reads restaurants and menus from the database. */
public final class MenuDAO {

    private MenuDAO() { }

    public static List<Restaurant> findRestaurants() throws SQLException {
        String sql = "SELECT restaurant_id, name FROM restaurants ORDER BY name";
        List<Restaurant> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Restaurant(rs.getInt("restaurant_id"), rs.getString("name")));
            }
        }
        return list;
    }

    /**
     * All categories of a restaurant with their items (deleted items are skipped).
     * Categories without items are still returned, with an empty list.
     */
    public static List<MenuSection> findMenu(int restaurantId) throws SQLException {
        String sql = "SELECT c.category_id, c.name AS category_name, "
                + "       m.item_id, m.name AS item_name, m.price, m.image_path "
                + "FROM categories c "
                + "LEFT JOIN menu_items m ON m.category_id = c.category_id AND m.is_deleted = FALSE "
                + "WHERE c.restaurant_id = ? "
                + "ORDER BY c.category_id, m.name";

        Map<Integer, String> names = new LinkedHashMap<>();
        Map<Integer, List<MenuItem>> items = new LinkedHashMap<>();

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, restaurantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int categoryId = rs.getInt("category_id");
                    names.putIfAbsent(categoryId, rs.getString("category_name"));
                    List<MenuItem> list = items.computeIfAbsent(categoryId, k -> new ArrayList<>());

                    int itemId = rs.getInt("item_id");
                    if (!rs.wasNull()) { // LEFT JOIN gives NULL when the category has no items
                        list.add(new MenuItem(itemId, categoryId, rs.getString("item_name"),
                                rs.getBigDecimal("price"), rs.getString("image_path")));
                    }
                }
            }
        }

        List<MenuSection> sections = new ArrayList<>();
        for (Map.Entry<Integer, String> e : names.entrySet()) {
            sections.add(new MenuSection(e.getKey(), e.getValue(), items.get(e.getKey())));
        }
        return sections;
    }
}
