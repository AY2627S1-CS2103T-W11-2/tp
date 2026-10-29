package seedu.boothmanagerpro.logic.parser;

import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.logic.commands.FindCommand;
import seedu.boothmanagerpro.model.person.ContactMatchesFieldsPredicate;
import seedu.boothmanagerpro.model.person.FollowUpDate;

/** Validates date search criteria using the same calendar rules as add and update. */
public class FindFollowUpDateParserTest {
    private final FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_dateAlternatives_normalisesDeduplicatesAndCombinesFields() {
        FindCommand expected = new FindCommand(new ContactMatchesFieldsPredicate(Map.of(
                "f/", Set.of("20261025", "20261026"), "c/", Set.of("technova"))));
        assertParseSuccess(parser, " f/20261025\t f/20261026 f/20261025 c/TechNova ", expected);
    }

    @Test
    public void parse_validCalendarBoundaries_accepts() {
        for (String date : List.of("00010101", "99991231", "20240229", "20261025")) {
            assertParseSuccess(parser, "f/" + date, new FindCommand(
                    new ContactMatchesFieldsPredicate(Map.of("f/", Set.of(date)))));
        }
    }

    @Test
    public void parse_invalidCalendarDateOrFormat_rejects() {
        for (String date : List.of("20230229", "20260431", "20261301", "00000101", "2026-10-25",
                "2026102", "202610250", "none", "２０２６１０２５", "2026 1025", "20261025 extra")) {
            assertParseFailure(parser, "f/" + date, "Invalid value for f/: " + FollowUpDate.MESSAGE_CONSTRAINTS);
        }
        assertParseFailure(parser, "f/20261025 f/20230229",
                "Invalid value for f/: " + FollowUpDate.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_emptyOrMixedUnprefixedValue_rejects() {
        for (String input : List.of("f/", "f/   ", "f/20261025 f/", "f/ c/TechNova")) {
            assertParseFailure(parser, input, FindCommandParser.MESSAGE_EMPTY_VALUE);
        }
        assertParseFailure(parser, "Alicia f/20261025", FindCommandParser.MESSAGE_UNPREFIXED_VALUE);
        assertParseFailure(parser, "f/20261025 x/Completed", FindCommandParser.MESSAGE_UNSUPPORTED_FIELD);
        assertParseSuccess(parser, "f/20261025 s/Completed m/email", new FindCommand(
                new ContactMatchesFieldsPredicate(Map.of(
                        "f/", Set.of("20261025"), "s/", Set.of("completed"), "m/", Set.of("email")))));
    }
}
