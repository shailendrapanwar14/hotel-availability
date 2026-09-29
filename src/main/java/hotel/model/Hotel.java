package hotel.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A hotel and its physical room inventory.
 *
 * <p>The {@code roomTypes} array in the JSON (descriptions, amenities, features)
 * is ignored; only the {@code rooms} array is needed to count available rooms.
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)} makes this explicit and
 * prevents failures if the JSON schema gains new fields in future.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Hotel(
        @JsonProperty("id")    String id,
        @JsonProperty("name")  String name,
        @JsonProperty("rooms") List<Room> rooms
) {
    /** Total physical rooms of the given type at this hotel. */
    public long roomCountForType(String roomType) {
        return rooms.stream()
                .filter(r -> r.roomType().equals(roomType))
                .count();
    }
}
