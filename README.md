# Hotel Availability

A command-line tool that reads hotel and booking data from JSON files
and answers room-availability queries.

## Build

Requires **JDK 21**.  Maven is bundled via the Maven Wrapper — no local
Maven installation is needed.

### Run tests

```bash
# Linux / macOS
./mvnw test

# Windows
mvnw.cmd test
```

### Build runnable JAR

```bash
./mvnw package -DskipTests
```

This produces a self-contained fat JAR at `target/hotel-availability-1.0.0.jar`.

## Run

```bash
java -jar target/hotel-availability-1.0.0.jar --hotels hotels.json --bookings bookings.json
```

The program reads commands from standard input, one per line, and writes
one result line per command to standard output.  It stops on a blank line
or end of input.  Diagnostic / log output is written to **stderr** so it
never pollutes the result stream.

### Example

```
$ java -jar target/hotel-availability-1.0.0.jar --hotels hotels.json --bookings bookings.json
Availability(H1, 20240901, SGL)
2
Availability(H1, 20240901-20240903, DBL)
1
Search(H1, 365, SGL)
(20240901-20240902, 2), (20240902-20240903, 1), (20240904-20240906, 1), (20240906-20250901, 2)

```

## Commands

| Command | Returns |
|---|---|
| `Availability(hotelId, date, roomType)` | Integer — free rooms on that day; negative if overbooked |
| `Availability(hotelId, startDate-endDate, roomType)` | Integer — minimum free rooms across the range |
| `Search(hotelId, daysAhead, roomType)` | Comma-separated `(startDate-endDate, count)` ranges with positive availability, or an empty line |

## Assumptions & Design Decisions

- **Date range semantics**: bookings occupy `[arrival, departure)` — the guest
  checks out on the departure morning so that day is free.  Query date ranges
  follow the same convention; a single-date query is normalised to a one-day
  range internally.
- **Range availability** is the *minimum* daily availability across the range,
  since a multi-night stay is only possible if rooms are free on every night.
- **Search** omits days with zero or negative availability and merges
  consecutive days sharing the same positive count into a single range.
- **Overbooked** days (more bookings than rooms) yield a negative availability
  value, as required by the spec.
- **Unknown hotel IDs** are treated as having zero rooms; availability equals
  the negative of any matching bookings (zero if no bookings exist for that ID).
- **Graceful error handling**: an unrecognised or malformed command is logged to
  stderr and skipped; the process does not abort.
- **JSON parsing** is handled by Jackson (`jackson-databind`) rather than a
  hand-rolled parser, for correctness and maintainability.

## Running Tests

```bash
./mvnw test          # runs all JUnit 5 tests
./mvnw test -pl .    # same, scoped to this module
```

There are **24 tests** in total:

| Class | Tests |
|---|---|
| `AvailabilityServiceTest` | 18 spec examples + 5 edge cases |
| `CommandParserTest` | 11 parser correctness + validation cases |

## Project Structure

```
pom.xml                             Maven build (JUnit 5, Jackson, Logback)
src/main/java/hotel/
  Main.java                         CLI entry point; orchestrates loading & I/O
  command/
    Command.java                    Sealed interface for typed commands
    AvailabilityCommand.java        Record for Availability queries
    SearchCommand.java              Record for Search queries
    CommandParser.java              Parses raw command strings
  exception/
    InvalidCommandException.java    Thrown for bad command syntax
  model/
    Hotel.java                      Hotel + room inventory (Jackson record)
    Room.java                       Physical room (Jackson record)
    Booking.java                    Booking with occupies-date logic (Jackson record)
  service/
    AvailabilityService.java        Core availability + search logic
    DataLoader.java                 Jackson-based JSON loader
  util/
    DateUtils.java                  Shared yyyyMMdd parsing / formatting
  JsonParser.java                   @Deprecated — superseded by Jackson
src/main/resources/
  logback.xml                       Logback config (stderr, INFO level)
src/test/java/hotel/
  service/AvailabilityServiceTest.java  18 spec cases + edge cases (JUnit 5)
  command/CommandParserTest.java        CommandParser unit tests (JUnit 5)
hotels.json, bookings.json          Sample data from the spec
```

## LLM Usage Note

This solution was developed with AI assistance. All logic has been verified
against the worked examples in the specification and I understand the
resulting code fully.
