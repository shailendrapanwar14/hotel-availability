package hotel.util;

import hotel.exception.InvalidCommandException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Shared date-parsing and formatting helpers.
 *
 * <p>Centralising the pattern here means it is defined exactly once, and any
 * caller that receives a malformed date string gets a meaningful
 * {@link InvalidCommandException} instead of a raw {@link DateTimeParseException}.
 */
public final class DateUtils {

    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private DateUtils() {}

    public static LocalDate parse(String yyyyMMdd) {
        try {
            return LocalDate.parse(yyyyMMdd.trim(), FORMAT);
        } catch (DateTimeParseException e) {
            throw new InvalidCommandException(
                    "Invalid date '" + yyyyMMdd + "' — expected yyyyMMdd format", e);
        }
    }

    public static String format(LocalDate date) {
        return date.format(FORMAT);
    }
}
