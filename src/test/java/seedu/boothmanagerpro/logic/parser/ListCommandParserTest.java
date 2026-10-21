package seedu.boothmanagerpro.logic.parser;

import static seedu.boothmanagerpro.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_ORDER;
import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.logic.Messages;
import seedu.boothmanagerpro.logic.commands.ListCommand;
import seedu.boothmanagerpro.model.person.SortField;

public class ListCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE);
    private static final String MESSAGE_UNKNOWN_FIELD =
            SortField.MESSAGE_CONSTRAINTS + "\n" + ListCommand.MESSAGE_USAGE;

    private final ListCommandParser parser = new ListCommandParser();

    @Test
    public void parse_noParameters_returnsStoredOrder() {
        assertParseSuccess(parser, "", new ListCommand());
        assertParseSuccess(parser, "   ", new ListCommand());
    }

    @Test
    public void parse_validSortField_returnsSortedList() {
        assertParseSuccess(parser, " o/name", new ListCommand(SortField.NAME));
        assertParseSuccess(parser, " o/company", new ListCommand(SortField.COMPANY));
        assertParseSuccess(parser, "  o/ Company  ", new ListCommand(SortField.COMPANY));
    }

    @Test
    public void parse_textOutsideOrder_throwsParseException() {
        assertParseFailure(parser, " 3", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " name", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " n/Alex", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, " 3 o/name", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_unknownSortField_throwsParseException() {
        assertParseFailure(parser, " o/", MESSAGE_UNKNOWN_FIELD);
        assertParseFailure(parser, " o/email", MESSAGE_UNKNOWN_FIELD);
        assertParseFailure(parser, " o/name company", MESSAGE_UNKNOWN_FIELD);
    }

    @Test
    public void parse_repeatedOrder_throwsParseException() {
        assertParseFailure(parser, " o/name o/company", Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ORDER));
        assertParseFailure(parser, " o/name o/name", Messages.getErrorMessageForDuplicatePrefixes(PREFIX_ORDER));
    }
}
