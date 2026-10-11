package com.tastybite.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** How dates are written on the manager screens. */
final class OrderFormat {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);

    private OrderFormat() { }

    static String time(LocalDateTime t) {
        return t == null ? "-" : t.format(WHEN);
    }
}
