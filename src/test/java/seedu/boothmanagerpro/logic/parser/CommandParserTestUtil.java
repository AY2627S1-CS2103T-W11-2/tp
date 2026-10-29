package seedu.boothmanagerpro.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import seedu.boothmanagerpro.logic.commands.Command;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;

/**
 * Contains helper methods for testing command parsers.
 */
public class CommandParserTestUtil {

    /**
     * Asserts that the parsing of {@code userInput} by {@code parser} is successful and the command created
     * is equal to {@code expectedCommand}.
     */
    public static void assertParseSuccess(Parser<? extends Command> parser, String userInput,
            Command expectedCommand) {
        try {
            Command command = parser.parse(userInput);
            assertEquals(expectedCommand, command);
        } catch (ParseException pe) {
            throw new IllegalArgumentException("Invalid userInput.", pe);
        }
    }

    /**
     * Asserts that the parsing of {@code userInput} by {@code parser} is unsuccessful and the error message
     * is equal to {@code expectedMessage}.
     */
    public static void assertParseFailure(Parser<? extends Command> parser, String userInput, String expectedMessage) {
        try {
            parser.parse(userInput);
            throw new AssertionError("The expected ParseException was not thrown.");
        } catch (ParseException pe) {
            assertEquals(expectedMessage, pe.getMessage());
        }
    }

    /**
     * Asserts that zero, negative and overflowing indices placed before {@code suffix} are rejected with the
     * specific index messages rather than a name or format error.
     */
    public static void assertInvalidIndexFailure(Parser<? extends Command> parser, String suffix) {
        for (String index : List.of("0", "-1", "+1", "000")) {
            assertParseFailure(parser, index + suffix, "Index must be a positive integer.");
        }
        for (String index : List.of("2147483648", "99999999999999")) {
            assertParseFailure(parser, index + suffix,
                    "Index is too large. Use a number shown in the current list, find or view results.");
        }
    }

    /**
     * Asserts that arguments starting with {@code n/} are rejected with the message naming {@code commandWord}
     * and its correct {@code usage}.
     */
    public static void assertNamePrefixFailure(Parser<? extends Command> parser, String args, String commandWord,
            String usage) {
        assertParseFailure(parser, args, "Don't include n/ for " + commandWord + ". Use: " + usage);
    }
}
