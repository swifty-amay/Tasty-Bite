package com.tastybite.service;

import com.tastybite.dao.ProfileDAO;
import com.tastybite.model.Account;
import com.tastybite.util.PasswordUtil;
import com.tastybite.util.Session;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

/** Module B3: changing the logged-in person's username and/or password. */
public final class ProfileService {

    private ProfileService() { }

    /** What was actually changed, so the screen can say the right thing. */
    public record Result(boolean usernameChanged, boolean passwordChanged) { }

    /**
     * Saves new details for the logged-in account.
     * The current password is always required. A blank newPassword keeps the old password.
     */
    public static Result update(String newUsername, String currentPassword,
                                String newPassword, String confirmPassword) throws AuthException {
        Account account = Session.getCurrent();
        if (account == null) {
            throw new AuthException("Please log in again.");
        }

        newUsername = newUsername == null ? "" : newUsername.trim();
        boolean changePassword = !newPassword.isEmpty();

        if (currentPassword.isEmpty()) {
            throw new AuthException("Enter your current password to save changes.");
        }
        AuthService.checkUsername(newUsername);
        if (changePassword) {
            AuthService.checkPassword(newPassword);
            if (!newPassword.equals(confirmPassword)) {
                throw new AuthException("The new passwords do not match.");
            }
        }
        boolean usernameChanged = !newUsername.equals(account.username());
        if (!usernameChanged && !changePassword) {
            throw new AuthException("You have not changed anything.");
        }

        try {
            ProfileDAO.Row row = ProfileDAO.findById(account.role(), account.id())
                    .orElseThrow(() -> new AuthException("Account not found. Please log in again."));
            if (!PasswordUtil.verify(currentPassword, row.hash())) {
                throw new AuthException("Your current password is incorrect.");
            }
            if (usernameChanged && ProfileDAO.usernameTakenByOther(account.role(), newUsername, account.id())) {
                throw new AuthException("That username is already taken.");
            }

            ProfileDAO.update(account.role(), account.id(), newUsername,
                    changePassword ? PasswordUtil.hash(newPassword) : null);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AuthException("That username is already taken.");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new AuthException("Database error. Please check the connection and try again.");
        }

        if (usernameChanged) {
            Session.updateAccount(new Account(account.id(), newUsername, account.role(), account.restaurant()));
        }
        return new Result(usernameChanged, changePassword);
    }
}
