package com.tastybite.model;

/** The two kinds of login. Each role maps to its own table. */
public enum Role {
    USER("users", "user_id", "User"),
    MANAGER("managers", "manager_id", "Manager");

    private final String table;
    private final String idColumn;
    private final String label;

    Role(String table, String idColumn, String label) {
        this.table = table;
        this.idColumn = idColumn;
        this.label = label;
    }

    public String table() { return table; }

    public String idColumn() { return idColumn; }

    public String label() { return label; }
}
