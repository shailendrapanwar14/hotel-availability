package hotel;

import hotel.command.AvailabilityCommand;
import hotel.command.Command;
import hotel.command.CommandParser;
import hotel.command.SearchCommand;
import hotel.exception.InvalidCommandException;
import hotel.model.Booking;
import hotel.model.Hotel;
import hotel.service.AvailabilityService;
import hotel.service.DataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Entry point for the hotel availability tool.
 *
 * <pre>
 *   java -jar hotel-availability.jar --hotels hotels.json --bookings bookings.json
 * </pre>
 *
 * <p>Reads commands from stdin, one per line, and writes one result per command
 * to stdout.  Stops on a blank line or EOF.  An invalid command line is logged
 * and skipped rather than crashing the process.
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Map<String, String> options = parseArgs(args);
        String hotelsPath   = options.get("hotels");
        String bookingsPath = options.get("bookings");

        if (hotelsPath == null || bookingsPath == null) {
            System.err.println(
                "Usage: java -jar hotel-availability.jar --hotels <file> --bookings <file>");
            System.exit(1);
        }

        try {
            DataLoader loader = new DataLoader();
            Map<String, Hotel> hotelsById = loader.loadHotels(Path.of(hotelsPath));
            List<Booking>      bookings   = loader.loadBookings(Path.of(bookingsPath));

            log.info("Loaded {} hotel(s) and {} booking(s)", hotelsById.size(), bookings.size());

            AvailabilityService service = new AvailabilityService(hotelsById, bookings);
            processCommands(service, System.in, System.out);

        } catch (IOException e) {
            log.error("Failed to load data files", e);
            System.err.println("Error loading data: " + e.getMessage());
            System.exit(1);
        }
    }

    // ── Package-private for unit testing ─────────────────────────────────────

    static Map<String, String> parseArgs(String[] args) {
        Map<String, String> options = new LinkedHashMap<>();
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].startsWith("--")) {
                options.put(args[i].substring(2), args[i + 1]);
            }
        }
        return options;
    }

    static void processCommands(AvailabilityService service,
                                InputStream in,
                                PrintStream out) throws IOException {
        BufferedReader reader =
            new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isBlank()) break;
            try {
                Command command = CommandParser.parse(line.trim());
                out.println(execute(service, command));
            } catch (InvalidCommandException e) {
                // Log and continue — an invalid line should not abort the session
                log.warn("Skipping unrecognised command '{}': {}", line.trim(), e.getMessage());
                System.err.println("Invalid command skipped: " + e.getMessage());
            }
        }
    }

    /**
     * Executes a parsed command and returns its string result.
     *
     * <p>The pattern-matching {@code switch} on the sealed {@link Command}
     * interface is exhaustive — the compiler will flag a missing case if a new
     * command type is added without updating this method.
     */
    static String execute(AvailabilityService service, Command command) {
        return switch (command) {
            case AvailabilityCommand a ->
                String.valueOf(service.availabilityForRange(
                        a.hotelId(), a.start(), a.endExclusive(), a.roomType()));
            case SearchCommand s ->
                String.join(", ", service.search(
                        s.hotelId(), s.daysAhead(), s.roomType(), LocalDate.now()));
        };
    }
}
