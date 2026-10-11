package com.tastybite.model;

/** The three states of an order. The name matches the ENUM stored in the orders table. */
public enum OrderStatus {
    PLACED("Pending"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String label;

    OrderStatus(String label) { this.label = label; }

    /** Text shown on screen. */
    public String label() { return label; }
}
