package com.tastybite.model;

import java.math.BigDecimal;
import java.util.List;

/** Everything shown by "View Order". */
public record OrderDetail(OrderSummary summary, BigDecimal subtotal, BigDecimal tax, List<OrderLine> lines) { }
