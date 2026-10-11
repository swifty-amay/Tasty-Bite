package com.tastybite.model;

import java.util.List;

/** A heading such as "Snacks" together with its items. */
public record MenuSection(int categoryId, String name, List<MenuItem> items) { }
