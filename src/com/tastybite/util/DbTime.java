package com.tastybite.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Reads a TIMESTAMP column as the person's local time, correctly in every time zone.
 *
 * The queries select UNIX_TIMESTAMP(column), which is the exact moment in time (seconds since 1970, UTC)
 * no matter which time zone the database server or the JDBC driver uses. The moment is then shown in the
 * time zone of the computer running the app. That avoids the driver's own time-zone guessing, which can
 * shift times by the zone offset (for example +5:30 in India).
 */
public final class DbTime {

    private DbTime() { }

    /** @param epochColumn a column or alias that holds UNIX_TIMESTAMP(...); returns null for SQL NULL */
    public static LocalDateTime read(ResultSet rs, String epochColumn) throws SQLException {
        long seconds = rs.getLong(epochColumn);
        if (rs.wasNull()) return null;
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.systemDefault());
    }
}
