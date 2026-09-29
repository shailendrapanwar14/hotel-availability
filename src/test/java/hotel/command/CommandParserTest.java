package hotel.command;

import hotel.exception.InvalidCommandException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CommandParser")
class CommandParserTest {

    @Test
    @DisplayName("Parses single-date Availability — end date normalised to start+1")
    void singleDateAvailability() {
        var cmd = (AvailabilityCommand) CommandParser.parse("Availability(H1, 20240901, SGL)");
        assertThat(cmd.hotelId()).isEqualTo("H1");
        assertThat(cmd.roomType()).isEqualTo("SGL");
        assertThat(cmd.start()).isEqualTo(LocalDate.of(2024, 9, 1));
        assertThat(cmd.endExclusive()).isEqualTo(LocalDate.of(2024, 9, 2));
    }

    @Test
    @DisplayName("Parses date-range Availability")
    void rangeAvailability() {
        var cmd = (AvailabilityCommand) CommandParser.parse("Availability(H1, 20240901-20240903, DBL)");
        assertThat(cmd.start()).isEqualTo(LocalDate.of(2024, 9, 1));
        assertThat(cmd.endExclusive()).isEqualTo(LocalDate.of(2024, 9, 3));
        assertThat(cmd.roomType()).isEqualTo("DBL");
    }

    @Test
    @DisplayName("Parses Search command")
    void searchCommand() {
        var cmd = (SearchCommand) CommandParser.parse("Search(H1, 365, SGL)");
        assertThat(cmd.hotelId()).isEqualTo("H1");
        assertThat(cmd.daysAhead()).isEqualTo(365);
        assertThat(cmd.roomType()).isEqualTo("SGL");
    }

    @Test
    @DisplayName("Trims whitespace around arguments")
    void trimsWhitespace() {
        var cmd = (SearchCommand) CommandParser.parse("Search( H2 ,  7 , DBL )");
        assertThat(cmd.hotelId()).isEqualTo("H2");
        assertThat(cmd.daysAhead()).isEqualTo(7);
        assertThat(cmd.roomType()).isEqualTo("DBL");
    }

    @ParameterizedTest
    @DisplayName("Throws InvalidCommandException for malformed inputs")
    @ValueSource(strings = {
        "",
        "   ",
        "Unknown(H1, 20240901, SGL)",
        "Availability(H1)",
        "Search(H1, abc, SGL)",
        "Search(H1, 0, SGL)",
        "Search(H1, -1, SGL)"
    })
    void throwsOnMalformedInput(String input) {
        assertThatThrownBy(() -> CommandParser.parse(input))
                .isInstanceOf(InvalidCommandException.class);
    }

    @Test
    @DisplayName("Throws when Availability end date is not after start date")
    void throwsWhenEndNotAfterStart() {
        assertThatThrownBy(() -> CommandParser.parse("Availability(H1, 20240903-20240901, SGL)"))
                .isInstanceOf(InvalidCommandException.class)
                .hasMessageContaining("Start date must be before end date");
    }

    @Test
    @DisplayName("Throws when date format is invalid")
    void throwsOnInvalidDate() {
        assertThatThrownBy(() -> CommandParser.parse("Availability(H1, 2024-09-01, SGL)"))
                .isInstanceOf(InvalidCommandException.class)
                .hasMessageContaining("Invalid date");
    }
}
