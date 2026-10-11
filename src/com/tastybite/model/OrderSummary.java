package com.tastybite.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** One row of the Latest Orders list or the History list. */
public record OrderSummary(int orderId, int userId, String username, BigDecimal total,
                           OrderStatus status, LocalDateTime placedAt, LocalDateTime updatedAt) { }
