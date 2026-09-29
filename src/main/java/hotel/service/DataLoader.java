package hotel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import hotel.model.Booking;
import hotel.model.Hotel;
import hotel.util.DateUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Loads hotel and booking data from JSON files using Jackson.
 *
 * <p>Jackson handles the deserialization boilerplate that the hand-rolled
 * parser previously covered, while being well-tested, standard, and able
 * to handle edge-cases (unicode escapes, nested structures, etc.) correctly.
 */
public class DataLoader {

    private final ObjectMapper mapper;

    public DataLoader() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // Teach Jackson our yyyyMMdd date format for LocalDate fields
        javaTimeModule.addDeserializer(LocalDate.class,
                new LocalDateDeserializer(DateUtils.FORMAT));
        javaTimeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateUtils.FORMAT));

        this.mapper = new ObjectMapper()
                .registerModule(javaTimeModule)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * @return hotels keyed by their ID, in document order.
     * @throws IllegalStateException if the file contains duplicate hotel IDs.
     */
    public Map<String, Hotel> loadHotels(Path path) throws IOException {
        Hotel[] hotels = mapper.readValue(path.toFile(), Hotel[].class);
        return Arrays.stream(hotels)
                .collect(Collectors.toMap(
                        Hotel::id,
                        Function.identity(),
                        (a, b) -> { throw new IllegalStateException("Duplicate hotel id: " + a.id()); },
                        LinkedHashMap::new));
    }

    public List<Booking> loadBookings(Path path) throws IOException {
        return List.of(mapper.readValue(path.toFile(), Booking[].class));
    }
}
