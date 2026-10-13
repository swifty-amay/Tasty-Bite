package com.tastybite.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** How dates are written on the manager screens. */
final class OrderFormat {

    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ENGLISH);

    private static final DateTimeFormatter SHORT = DateTimeFormatter.ofPattern("dd MMM, hh:mm a", Locale.ENGLISH);

    private OrderFormat() { }

    /** Without the year, for narrow columns. */
    static String timeShort(LocalDateTime t) {
        return t == null ? "-" : t.format(SHORT);
    }

    static String time(LocalDateTime t) {
        return t == null ? "-" : t.format(WHEN);
    }
}
