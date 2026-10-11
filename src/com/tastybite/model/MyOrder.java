package com.tastybite.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** One of the customer's own orders, with everything needed to show it on the My Orders screen. */
public record MyOrder(int orderId, String restaurantName, BigDecimal subtotal, BigDecimal tax,
                      BigDecimal total, OrderStatus status, LocalDateTime placedAt,
                      LocalDateTime updatedAt, List<OrderLine> lines) { }
