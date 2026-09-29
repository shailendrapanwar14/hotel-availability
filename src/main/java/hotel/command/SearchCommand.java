package hotel.command;

/**
 * Parsed representation of a {@code Search(hotelId, daysAhead, roomType)} command.
 */
public record SearchCommand(
        String hotelId,
        int daysAhead,
        String roomType
) implements Command {}
