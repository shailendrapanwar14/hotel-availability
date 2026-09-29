package hotel.service;

import hotel.model.Booking;
import hotel.model.Hotel;
import hotel.model.Room;
import hotel.util.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link AvailabilityService} covering all 18 examples from
 * the specification, plus additional edge cases.
 *
 * <p>The service is constructed directly with in-memory data — no file I/O,
 * no mocking required.  Using {@code @BeforeEach} means each test starts from
 * a clean service instance.
 */
@DisplayName("AvailabilityService")
class AvailabilityServiceTest {

    private AvailabilityService service;

    // Pinned reference dates used across multiple tests
    private static final LocalDate SEPT_1 = LocalDate.of(2024, 9, 1);
    private static final LocalDate SEPT_3 = LocalDate.of(2024, 9, 3);

    @BeforeEach
    void setUp() {
        Map<String, Hotel> hotels = Map.of(
                "H1", new Hotel("H1", "Hotel California", List.of(
                        new Room("SGL", "101"),
                        new Room("SGL", "102"),
                        new Room("DBL", "201"),
                        new Room("DBL", "202")
                )),
                "H2", new Hotel("H2", "Downtown Suites", List.of(
                        new Room("SGL", "101")
                ))
        );

        List<Booking> bookings = List.of(
                new Booking("H1", d("20240901"), d("20240903"), "DBL", "Prepaid"),
                new Booking("H1", d("20240902"), d("20240905"), "SGL", "Standard"),
                new Booking("H1", d("20240905"), d("20240906"), "SGL", "Standard"),
                new Booking("H1", d("20240903"), d("20240904"), "SGL", "Prepaid"),
                new Booking("H1", d("20240903"), d("20240904"), "SGL", "Prepaid"),
                new Booking("H2", d("20240901"), d("20240907"), "SGL", "Standard")
        );

        service = new AvailabilityService(hotels, bookings);
    }

    // ── Spec examples: single-day Availability ───────────────────────────────

    @Nested
    @DisplayName("Single-day Availability (spec cases 1-6)")
    class SingleDay {

        @Test @DisplayName("Case 1 — H1 SGL 2024-09-01: no SGL bookings yet → 2")
        void case1() { assertAvail("H1", "20240901", "20240902", "SGL", 2); }

        @Test @DisplayName("Case 2 — H1 DBL 2024-09-01: 1 of 2 booked → 1")
        void case2() { assertAvail("H1", "20240901", "20240903", "DBL", 1); }

        @Test @DisplayName("Case 3 — H1 SGL 2024-09-02: 1 of 2 booked → 1")
        void case3() { assertAvail("H1", "20240902", "20240903", "SGL", 1); }

        @Test @DisplayName("Case 4 — H1 SGL 2024-09-03: overbooked (3 vs 2 rooms) → -1")
        void case4() { assertAvail("H1", "20240903", "20240904", "SGL", -1); }

        @Test @DisplayName("Case 5 — H1 SGL 2024-09-04: 1 of 2 booked → 1")
        void case5() { assertAvail("H1", "20240904", "20240905", "SGL", 1); }

        @Test @DisplayName("Case 6 — H1 SGL 2024-09-06: no bookings → 2")
        void case6() { assertAvail("H1", "20240906", "20240907", "SGL", 2); }
    }

    // ── Spec examples: range Availability ────────────────────────────────────

    @Nested
    @DisplayName("Range Availability (spec cases 7-12)")
    class Range {

        @Test @DisplayName("Case 7 — H1 SGL [Sep 02, Sep 03) → min 1")
        void case7() { assertAvail("H1", "20240902", "20240903", "SGL", 1); }

        @Test @DisplayName("Case 8 — H1 SGL [Sep 02, Sep 05): overbooking on Sep 03 → min -1")
        void case8() { assertAvail("H1", "20240902", "20240905", "SGL", -1); }

        @Test @DisplayName("Case 9 — H1 SGL [Sep 04, Sep 06) → min 1")
        void case9() { assertAvail("H1", "20240904", "20240906", "SGL", 1); }

        @Test @DisplayName("Case 10 — H1 DBL [Sep 03, Sep 04): booking ended, 2 free → 2")
        void case10() { assertAvail("H1", "20240903", "20240904", "DBL", 2); }

        @Test @DisplayName("Case 11 — H2 SGL [Sep 01, Sep 02): fully booked → 0")
        void case11() { assertAvail("H2", "20240901", "20240902", "SGL", 0); }

        @Test @DisplayName("Case 12 — H2 SGL [Sep 07, Sep 08): booking ends Sep 07 → 1")
        void case12() { assertAvail("H2", "20240907", "20240908", "SGL", 1); }
    }

    // ── Spec examples: Search ────────────────────────────────────────────────

    @Nested
    @DisplayName("Search (spec cases 1-6)")
    class Search {

        @Test @DisplayName("Search 1 — H1 SGL 365 days from Sep 01: 4 ranges")
        void search1() {
            assertThat(service.search("H1", 365, "SGL", SEPT_1)).containsExactly(
                    "(20240901-20240902, 2)",
                    "(20240902-20240903, 1)",
                    "(20240904-20240906, 1)",
                    "(20240906-20250901, 2)"
            );
        }

        @Test @DisplayName("Search 2 — H1 SGL 1 day from Sep 01: only Sep 01")
        void search2() {
            assertThat(service.search("H1", 1, "SGL", SEPT_1))
                    .containsExactly("(20240901-20240902, 2)");
        }

        @Test @DisplayName("Search 3 — H1 SGL 4 days from Sep 01: Sep 03 skipped (overbooked)")
        void search3() {
            assertThat(service.search("H1", 4, "SGL", SEPT_1)).containsExactly(
                    "(20240901-20240902, 2)",
                    "(20240902-20240903, 1)",
                    "(20240904-20240905, 1)"
            );
        }

        @Test @DisplayName("Search 4 — H2 SGL 7 days from Sep 01: only Sep 07")
        void search4() {
            assertThat(service.search("H2", 7, "SGL", SEPT_1))
                    .containsExactly("(20240907-20240908, 1)");
        }

        @Test @DisplayName("Search 5 — H2 SGL 1 day from Sep 01: fully booked, empty result")
        void search5() {
            assertThat(service.search("H2", 1, "SGL", SEPT_1)).isEmpty();
        }

        @Test @DisplayName("Search 6 — H1 SGL 1 day from Sep 03: overbooked, empty result")
        void search6() {
            assertThat(service.search("H1", 1, "SGL", SEPT_3)).isEmpty();
        }
    }

    // ── Edge cases ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test @DisplayName("Unknown hotel ID → 0 rooms, availability equals negative of bookings (0 if none)")
        void unknownHotel() {
            assertThat(service.availabilityForRange(
                    "UNKNOWN", SEPT_1, SEPT_1.plusDays(1), "SGL")).isEqualTo(0);
        }

        @Test @DisplayName("Unknown room type → 0 rooms of that type, all free")
        void unknownRoomType() {
            assertThat(service.availabilityForRange(
                    "H1", SEPT_1, SEPT_1.plusDays(1), "SUITE")).isEqualTo(0);
        }

        @Test @DisplayName("Departure date is exclusive — booking H2/Sep01-Sep07 frees Sep 07")
        void departureDateIsExclusive() {
            assertThat(service.availabilityForDay("H2", d("20240907"), "SGL")).isEqualTo(1);
            assertThat(service.availabilityForDay("H2", d("20240906"), "SGL")).isEqualTo(0);
        }

        @Test @DisplayName("Overbooked day returns negative availability")
        void overbookedIsNegative() {
            assertThat(service.availabilityForDay("H1", d("20240903"), "SGL")).isNegative();
        }

        @Test @DisplayName("Search returns empty list when no days have positive availability")
        void searchEmptyWhenAllBooked() {
            assertThat(service.search("H2", 3, "SGL", SEPT_1)).isEmpty();
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void assertAvail(String hotelId, String start, String end, String type, int expected) {
        assertThat(service.availabilityForRange(hotelId, d(start), d(end), type))
                .as("availabilityForRange(%s, %s, %s, %s)", hotelId, start, end, type)
                .isEqualTo(expected);
    }

    private static LocalDate d(String yyyyMMdd) {
        return DateUtils.parse(yyyyMMdd);
    }
}
