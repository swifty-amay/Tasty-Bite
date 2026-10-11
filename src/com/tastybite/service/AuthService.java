package com.tastybite.service;

import com.tastybite.dao.AuthDAO;
import com.tastybite.model.Account;
import com.tastybite.model.Restaurant;
import com.tastybite.model.Role;
import com.tastybite.util.PasswordUtil;
import com.tastybite.util.Session;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Optional;
import java.util.regex.Pattern;

/** Module B1: validation and rules for registration and login. Same flow for user and manager. */
public final class AuthService {

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_.]{3,30}$");
    private static final int MIN_PASSWORD = 6;

    private AuthService() { }

    public static void register(Role role, String username, String password, String confirm,
                                String restaurantName) throws AuthException {
        username = username == null ? "" : username.trim();
        restaurantName = restaurantName == null ? "" : restaurantName.trim();

        if (!USERNAME.matcher(username).matches()) {
            throw new AuthException("Username must be 3-30 characters: letters, numbers, _ or .");
        }
        if (password.length() < MIN_PASSWORD) {
            throw new AuthException("Password must be at least " + MIN_PASSWORD + " characters.");
        }
        if (!password.equals(confirm)) {
            throw new AuthException("Passwords do not match.");
        }
        if (role == Role.MANAGER && (restaurantName.length() < 2 || restaurantName.length() > 100)) {
            throw new AuthException("Enter a restaurant name (2-100 characters).");
        }

        try {
            if (AuthDAO.usernameExists(role, username)) {
                throw new AuthException("That username is already taken.");
            }
            if (role == Role.MANAGER && AuthDAO.restaurantNameExists(restaurantName)) {
                throw new AuthException("A restaurant with that name already exists.");
            }

            String hash = PasswordUtil.hash(password);
            if (role == Role.MANAGER) {
                AuthDAO.registerManager(username, hash, restaurantName);
            } else {
                AuthDAO.registerUser(username, hash);
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            // two people registering the same name at the same moment
            throw new AuthException("That username or restaurant name is already taken.");
        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    /** Checks the credentials and, on success, stores the account in the Session. */
    public static Account login(Role role, String username, String password) throws AuthException {
        username = username == null ? "" : username.trim();
        if (username.isEmpty() || password.isEmpty()) {
            throw new AuthException("Please enter your username and password.");
        }

        try {
            Optional<AuthDAO.Credential> cred = AuthDAO.findCredential(role, username);
            if (cred.isEmpty() || !PasswordUtil.verify(password, cred.get().hash())) {
                throw new AuthException("Invalid username or password.");
            }

            Restaurant restaurant = null;
            if (role == Role.MANAGER) {
                restaurant = AuthDAO.findRestaurantByManager(cred.get().id()).orElse(null);
            }

            Account account = new Account(cred.get().id(), cred.get().username(), role, restaurant);
            Session.login(account);
            return account;
        } catch (SQLException e) {
            throw databaseError(e);
        }
    }

    private static AuthException databaseError(SQLException e) {
        e.printStackTrace();
        return new AuthException("Database error. Please check the connection and try again.");
    }
}
