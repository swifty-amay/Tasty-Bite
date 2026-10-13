package com.tastybite.service;

import com.tastybite.dao.MenuDAO;
import com.tastybite.dao.MenuEditDAO;
import com.tastybite.model.Account;
import com.tastybite.model.MenuItem;
import com.tastybite.model.MenuSection;
import com.tastybite.model.Restaurant;
import com.tastybite.model.Role;
import com.tastybite.util.AppPaths;
import com.tastybite.util.Session;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Module B7: the manager adds, edits and removes items on their own restaurant's menu.
 * Photos are copied into the "menu_images" folder next to the app, and the relative path is stored,
 * so the menu keeps working even if the original file is moved or deleted.
 */
public final class MenuEditService {

    private static final Pattern PRICE = Pattern.compile("^\\d{1,5}(\\.\\d{1,2})?$");
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final File IMAGE_DIR = AppPaths.imageDir();

    private MenuEditService() { }

    /** Every section of the manager's menu, including empty ones. */
    public static List<MenuSection> getMenuForEditing() throws ServiceException {
        Restaurant restaurant = requireManagerRestaurant();
        try {
            return MenuDAO.findMenu(restaurant.id());
        } catch (SQLException e) {
            throw new ServiceException("Could not load the menu. Check the database connection.", e);
        }
    }

    /** Creates a new section such as "Desserts" on the manager's menu. */
    public static void addSection(String name) throws ServiceException {
        Restaurant restaurant = requireManagerRestaurant();
        String clean = checkSectionName(name);
        try {
            if (MenuEditDAO.categoryNameExists(restaurant.id(), clean, 0)) {
                throw new ServiceException("You already have a section called \"" + clean + "\".");
            }
            MenuEditDAO.insertCategory(restaurant.id(), clean);
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            throw new ServiceException("You already have a section called \"" + clean + "\".");
        } catch (SQLException e) {
            throw saveError(e);
        }
    }

    public static void renameSection(int categoryId, String name) throws ServiceException {
        Restaurant restaurant = requireManagerRestaurant();
        String clean = checkSectionName(name);
        try {
            if (!MenuEditDAO.categoryBelongsTo(categoryId, restaurant.id())) {
                throw new ServiceException("That section does not belong to your restaurant.");
            }
            if (MenuEditDAO.categoryNameExists(restaurant.id(), clean, categoryId)) {
                throw new ServiceException("You already have a section called \"" + clean + "\".");
            }
            MenuEditDAO.renameCategory(categoryId, restaurant.id(), clean);
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            throw new ServiceException("You already have a section called \"" + clean + "\".");
        } catch (SQLException e) {
            throw saveError(e);
        }
    }

    public static void addItem(int categoryId, String name, String priceText, File image)
            throws ServiceException {
        Restaurant restaurant = requireManagerRestaurant();
        String cleanName = checkName(name);
        BigDecimal price = parsePrice(priceText);

        try {
            if (!MenuEditDAO.categoryBelongsTo(categoryId, restaurant.id())) {
                throw new ServiceException("That section does not belong to your restaurant.");
            }
            if (MenuEditDAO.itemNameExists(restaurant.id(), categoryId, cleanName, 0)) {
                throw new ServiceException("This section already has an item called \"" + cleanName + "\".");
            }
            String imagePath = (image == null) ? null : storeImage(image, restaurant.id());
            MenuEditDAO.insertItem(restaurant.id(), categoryId, cleanName, price, imagePath);
        } catch (SQLException e) {
            throw saveError(e);
        }
    }

    /**
     * @param newImage    a new photo to use, or null to keep the current one
     * @param removeImage true to remove the photo (ignored when newImage is given)
     */
    public static void updateItem(int itemId, String name, String priceText, File newImage,
                                  boolean removeImage) throws ServiceException {
        Restaurant restaurant = requireManagerRestaurant();
        String cleanName = checkName(name);
        BigDecimal price = parsePrice(priceText);

        try {
            MenuItem existing = MenuEditDAO.findItem(itemId, restaurant.id())
                    .orElseThrow(() -> new ServiceException("That item no longer exists."));
            if (MenuEditDAO.itemNameExists(restaurant.id(), existing.categoryId(), cleanName, itemId)) {
                throw new ServiceException("This section already has an item called \"" + cleanName + "\".");
            }

            String imagePath = existing.imagePath();
            if (newImage != null) {
                imagePath = storeImage(newImage, restaurant.id());
            } else if (removeImage) {
                imagePath = null;
            }
            MenuEditDAO.updateItem(itemId, restaurant.id(), cleanName, price, imagePath);
        } catch (SQLException e) {
            throw saveError(e);
        }
    }

    /** Removes the item from the menu. Past orders keep their own copy of the name and price. */
    public static void deleteItem(int itemId) throws ServiceException {
        Restaurant restaurant = requireManagerRestaurant();
        try {
            MenuEditDAO.findItem(itemId, restaurant.id())
                    .orElseThrow(() -> new ServiceException("That item no longer exists."));
            MenuEditDAO.softDeleteItem(itemId, restaurant.id());
        } catch (SQLException e) {
            throw saveError(e);
        }
    }

    // ---------------------------------------------------------------- helpers

    private static Restaurant requireManagerRestaurant() throws ServiceException {
        Account account = Session.getCurrent();
        if (account == null || account.role() != Role.MANAGER || account.restaurant() == null) {
            throw new ServiceException("Only a restaurant manager can edit the menu.");
        }
        return account.restaurant();
    }

    private static String checkName(String name) throws ServiceException {
        String n = name == null ? "" : name.trim();
        if (n.isEmpty()) throw new ServiceException("Enter an item name.");
        if (n.length() > 100) throw new ServiceException("The item name must be 100 characters or fewer.");
        return n;
    }

    private static String checkSectionName(String name) throws ServiceException {
        String n = name == null ? "" : name.trim();
        if (n.isEmpty()) throw new ServiceException("Enter a section name.");
        if (n.length() > 50) throw new ServiceException("The section name must be 50 characters or fewer.");
        return n;
    }

    private static BigDecimal parsePrice(String text) throws ServiceException {
        String t = text == null ? "" : text.trim();
        if (!PRICE.matcher(t).matches()) {
            throw new ServiceException("Enter a valid price, for example 120 or 99.50.");
        }
        BigDecimal price = new BigDecimal(t);
        if (price.signum() <= 0) throw new ServiceException("The price must be greater than zero.");
        return price;
    }

    /** Checks the file is a real image, copies it into menu_images and returns the relative path. */
    private static String storeImage(File source, int restaurantId) throws ServiceException {
        String lower = source.getName().toLowerCase(Locale.ROOT);
        String ext = lower.endsWith(".jpeg") ? "jpg"
                : lower.endsWith(".jpg") ? "jpg"
                : lower.endsWith(".png") ? "png"
                : lower.endsWith(".gif") ? "gif" : null;
        if (ext == null) throw new ServiceException("Please choose a JPG, PNG or GIF image.");
        if (!source.isFile() || source.length() > MAX_IMAGE_BYTES) {
            throw new ServiceException("The image must be a file smaller than 5 MB.");
        }
        try {
            if (ImageIO.read(source) == null) {
                throw new ServiceException("That file is not a valid image.");
            }
            Files.createDirectories(IMAGE_DIR.toPath());
            String fileName = "r" + restaurantId + "_" + UUID.randomUUID().toString().substring(0, 12) + "." + ext;
            Files.copy(source.toPath(), new File(IMAGE_DIR, fileName).toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            return IMAGE_DIR.getName() + "/" + fileName;
        } catch (IOException e) {
            throw new ServiceException("Could not read that image file.", e);
        }
    }

    private static ServiceException saveError(SQLException e) {
        e.printStackTrace();
        return new ServiceException("Could not save your changes. Please try again.", e);
    }
}
