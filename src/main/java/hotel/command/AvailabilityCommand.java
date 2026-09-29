package hotel.command;

import java.time.LocalDate;

/**
 * Parsed representation of an {@code Availability(hotelId, date[range], roomType)} command.
 *
 * <p>Single-date queries are normalised to a half-open range
 * {@code [date, date+1)} by {@link CommandParser} before reaching here,
 * so the execution path is uniform regardless of the original input format.
 */
public record AvailabilityCommand(
        String hotelId,
        LocalDate start,
        LocalDate endExclusive,
        String roomType
) implements Command {}
