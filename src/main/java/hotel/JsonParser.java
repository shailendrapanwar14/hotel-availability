package hotel;

/**
 * @deprecated Superseded by Jackson ({@code jackson-databind}).
 *
 * <p>This class is retained only for reference. All JSON loading is now done
 * by {@link hotel.service.DataLoader}, which uses a properly configured
 * Jackson {@code ObjectMapper}. Jackson handles edge-cases (unicode escapes,
 * large numbers, malformed input, etc.) far more robustly than this
 * hand-rolled parser.
 *
 * <p>This class is not used anywhere in the application and will be removed
 * in a future cleanup commit.
 */
@Deprecated(since = "1.0.0", forRemoval = true)
final class JsonParser {
    private JsonParser() {}
}
