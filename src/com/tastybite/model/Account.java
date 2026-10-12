package com.tastybite.model;

/**
 * A logged-in person. restaurant is only set for managers.
 * For users the restaurant is chosen later (Module F2).
 */
public record Account(int id, String username, Role role, Restaurant restaurant) { }
