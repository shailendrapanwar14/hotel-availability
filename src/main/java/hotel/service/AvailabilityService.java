package hotel.service;

import hotel.model.Booking;
import hotel.model.Hotel;
import hotel.util.DateUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Core logic for answering {@code Availability} and {@code Search} queries.
 *
 * <p>All date ranges are <em>half-open</em>: {@code [start, endExclusive)},
 * matching the semantics of a hotel booking where the guest checks out on
 * the departure morning. A single-date query is normalised to
 * {@code [date, date+1)} by {@link hotel.command.CommandParser} before it
 * reaches this service.
 */
public class AvailabilityService {

    private final Map<String, Hotel>  hotelsById;
    private final List<Booking>       bookings;

    public AvailabilityService(Map<String, Hotel> hotelsById, List<Booking> bookings) {
        this.hotelsById = hotelsById;
        this.bookings   = bookings;
    }

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Availability for a single day: total rooms of the given type minus the
     * number of bookings occupying that day.  Can be negative if overbooked.
     */
    public int availabilityForDay(String hotelId, LocalDate date, String roomType) {
        Hotel hotel     = hotelsById.get(hotelId);
        long totalRooms = (hotel == null) ? 0 : hotel.roomCountForType(roomType);
        long occupied   = bookings.stream()
                .filter(b -> b.hotelId().equals(hotelId))
                .filter(b -> b.roomType().equals(roomType))
                .filter(b -> b.occupiesDate(date))
                .count();
        return (int) (totalRooms - occupied);
    }

    /**
     * Minimum availability across every day in {@code [start, endExclusive)}.
     *
     * <p>A range is only bookable if rooms are free on <em>every</em> day
     * within it, so the minimum is the correct answer for a range query.
     */
    public int availabilityForRange(String hotelId,
                                    LocalDate start,
                                    LocalDate endExclusive,
                                    String roomType) {
        int min = Integer.MAX_VALUE;
        for (LocalDate day = start; day.isBefore(endExclusive); day = day.plusDays(1)) {
            min = Math.min(min, availabilityForDay(hotelId, day, roomType));
        }
        // Guard against an empty range (start == endExclusive)
        return (min == Integer.MAX_VALUE) ? 0 : min;
    }

    /**
     * Returns contiguous date ranges with <em>positive</em> availability over
     * the next {@code daysAhead} days starting from {@code today}.
     *
     * <p>Days with zero or negative availability are omitted.  Consecutive
     * days sharing the same positive count are merged into a single
     * {@code (YYYYMMDD-YYYYMMDD, count)} range string.
     */
    public List<String> search(String hotelId,
                               int daysAhead,
                               String roomType,
                               LocalDate today) {
        List<String> results  = new ArrayList<>();
        LocalDate rangeStart  = null;
        LocalDate rangeLastDay = null;
        int       rangeCount  = 0;

        LocalDate end = today.plusDays(daysAhead);
        for (LocalDate day = today; day.isBefore(end); day = day.plusDays(1)) {
            int avail = availabilityForDay(hotelId, day, roomType);

            boolean continuesRun = (rangeStart != null) && (avail == rangeCount);
            if (avail > 0 && continuesRun) {
                rangeLastDay = day;
            } else {
                if (rangeStart != null) {
                    results.add(formatRange(rangeStart, rangeLastDay, rangeCount));
                    rangeStart = null;
                }
                if (avail > 0) {
                    rangeStart   = day;
                    rangeLastDay = day;
                    rangeCount   = avail;
                }
            }
        }
        if (rangeStart != null) {
            results.add(formatRange(rangeStart, rangeLastDay, rangeCount));
        }
        return results;
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    private String formatRange(LocalDate start, LocalDate lastDay, int count) {
        // lastDay is inclusive; the end printed in the output is the *exclusive*
        // bound (i.e. the first day the next guest could arrive), so +1.
        return "(" + DateUtils.format(start)
             + "-" + DateUtils.format(lastDay.plusDays(1))
             + ", " + count + ")";
    }
}
