package seedu.boothmanagerpro.logic.parser;

import static seedu.boothmanagerpro.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertInvalidIndexFailure;
import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertNamePrefixFailure;
import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.boothmanagerpro.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.logic.commands.DeleteCommand;
import seedu.boothmanagerpro.model.person.Name;

/**
 * Contains unit tests for {@code DeleteCommandParser}.
 */
public class DeleteCommandParserTest {

    private DeleteCommandParser parser = new DeleteCommandParser();

    @Test
    public void parse_validArgs_returnsDeleteCommand() {
        assertParseSuccess(parser, "1", new DeleteCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, " Alice Pauline ", new DeleteCommand(new Name("Alice Pauline")));
        assertParseSuccess(parser, "Raj s/o Kumar", new DeleteCommand(new Name("Raj s/o Kumar")));
    }

    @Test
    public void parse_missingTarget_showsUsage() {
        for (String args : new String[]{"", "  "}) {
            assertParseFailure(parser, args,
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        }
    }

    @Test
    public void parse_invalidName_showsNameConstraints() {
        for (String args : new String[]{"Alice!", "John123", "...", "A".repeat(81)}) {
            assertParseFailure(parser, args, Name.MESSAGE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_unexpectedPrefix_failure() {
        assertParseFailure(parser, "c/TechNova",
                String.format(DeleteCommandParser.MESSAGE_UNKNOWN_FIELD, "c/"));
        assertParseFailure(parser, "1 t/vip",
                String.format(DeleteCommandParser.MESSAGE_UNKNOWN_FIELD, "t/"));
        assertParseFailure(parser, "Alex Yeoh n/New Name",
                String.format(DeleteCommandParser.MESSAGE_UNKNOWN_FIELD, "n/"));
    }

    @Test
    public void parse_invalidIndex_failure() {
        assertInvalidIndexFailure(parser, "");
        assertInvalidIndexFailure(parser, "  ");
    }

    @Test
    public void parse_namePrefix_failure() {
        for (String args : new String[]{"n/Alex Yeoh", " n/1", "n/"}) {
            assertNamePrefixFailure(parser, args, "delete", "delete NAME or delete INDEX");
        }
    }
}
