package hotel.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * A booking occupies its room type at a hotel for the half-open date interval
 * {@code [arrival, departure)} — the guest checks out on the departure morning,
 * so that day is free for the next guest.
 *
 * <p>Jackson deserialises the {@code arrival} and {@code departure} fields using
 * the {@code yyyyMMdd} format configured on the shared {@link hotel.service.DataLoader}
 * ObjectMapper; no per-field annotation is needed.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Booking(
        @JsonProperty("hotelId")   String    hotelId,
        @JsonProperty("arrival")   LocalDate arrival,
        @JsonProperty("departure") LocalDate departure,
        @JsonProperty("roomType")  String    roomType,
        @JsonProperty("roomRate")  String    roomRate
) {
    /** Returns true when this booking occupies a room on {@code date}. */
    public boolean occupiesDate(LocalDate date) {
        return !date.isBefore(arrival) && date.isBefore(departure);
    }
}
