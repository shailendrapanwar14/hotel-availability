package hotel.command;

import hotel.exception.InvalidCommandException;
import hotel.util.DateUtils;

import java.time.LocalDate;

/**
 * Parses raw command strings into typed {@link Command} instances.
 *
 * <p>Extracting parsing into its own class gives {@link hotel.Main} a single
 * responsibility (orchestration) and makes the parser independently testable.
 */
public final class CommandParser {

    private CommandParser() {}

    /**
     * @throws InvalidCommandException if the line is blank, unrecognised, or
     *                                  has wrong argument count / types
     */
    public static Command parse(String line) {
        if (line == null || line.isBlank()) {
            throw new InvalidCommandException("Command line must not be blank");
        }
        String trimmed = line.trim();
        if (trimmed.startsWith("Availability(") && trimmed.endsWith(")")) {
            return parseAvailability(trimmed);
        }
        if (trimmed.startsWith("Search(") && trimmed.endsWith(")")) {
            return parseSearch(trimmed);
        }
        throw new InvalidCommandException("Unrecognised command: " + trimmed);
    }

    // ── private helpers ──────────────────────────────────────────────────────

    private static AvailabilityCommand parseAvailability(String line) {
        String[] parts = extractParts(line, "Availability(", 3);
        String hotelId  = parts[0].trim();
        String dateSpec = parts[1].trim();
        String roomType = parts[2].trim();

        requireNonEmpty(hotelId,  "hotelId");
        requireNonEmpty(roomType, "roomType");

        LocalDate start, endExclusive;
        if (dateSpec.contains("-")) {
            String[] dates = dateSpec.split("-", 2);
            start        = DateUtils.parse(dates[0]);
            endExclusive = DateUtils.parse(dates[1]);
            if (!start.isBefore(endExclusive)) {
                throw new InvalidCommandException(
                        "Start date must be before end date, got: " + dateSpec);
            }
        } else {
            start        = DateUtils.parse(dateSpec);
            endExclusive = start.plusDays(1);
        }
        return new AvailabilityCommand(hotelId, start, endExclusive, roomType);
    }

    private static SearchCommand parseSearch(String line) {
        String[] parts = extractParts(line, "Search(", 3);
        String hotelId  = parts[0].trim();
        String daysStr  = parts[1].trim();
        String roomType = parts[2].trim();

        requireNonEmpty(hotelId,  "hotelId");
        requireNonEmpty(roomType, "roomType");

        int daysAhead;
        try {
            daysAhead = Integer.parseInt(daysStr);
        } catch (NumberFormatException e) {
            throw new InvalidCommandException("daysAhead must be an integer, got: " + daysStr, e);
        }
        if (daysAhead <= 0) {
            throw new InvalidCommandException("daysAhead must be positive, got: " + daysAhead);
        }
        return new SearchCommand(hotelId, daysAhead, roomType);
    }

    private static String[] extractParts(String line, String prefix, int expectedCount) {
        String content = line.substring(prefix.length(), line.length() - 1);
        String[] parts = content.split(",", expectedCount);
        if (parts.length != expectedCount) {
            throw new InvalidCommandException(
                    prefix.replace("(", "") + " requires " + expectedCount
                    + " arguments, got " + parts.length + " in: " + line);
        }
        return parts;
    }

    private static void requireNonEmpty(String value, String fieldName) {
        if (value.isEmpty()) {
            throw new InvalidCommandException(fieldName + " must not be empty");
        }
    }
}
