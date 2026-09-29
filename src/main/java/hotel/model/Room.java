package hotel.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A single physical room belonging to a hotel.
 *
 * <p>{@code @JsonProperty} annotations let Jackson map JSON field names to
 * record components without requiring a no-arg constructor.
 */
public record Room(
        @JsonProperty("roomType") String roomType,
        @JsonProperty("roomId")   String roomId
) {}
