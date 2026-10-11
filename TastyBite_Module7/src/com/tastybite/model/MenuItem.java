package com.tastybite.model;

import java.math.BigDecimal;

/** One dish or drink on a restaurant's menu. imagePath may be null. */
public record MenuItem(int id, int categoryId, String name, BigDecimal price, String imagePath) { }
