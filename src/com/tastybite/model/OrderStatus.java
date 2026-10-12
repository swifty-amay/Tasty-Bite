package com.tastybite.model;

/**
 * The states of an order: PLACED -> PREPARING -> COMPLETED, or CANCELLED.
 * The names match the ENUM stored in the orders table.
 */
public enum OrderStatus {
    PLACED("Pending"),
    PREPARING("Preparing"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String label;

    OrderStatus(String label) { this.label = label; }

    /** Text shown on screen. */
    public String label() { return label; }

    /** Pending or preparing: the order still needs the restaurant's attention. */
    public boolean isActive() { return this == PLACED || this == PREPARING; }
}
