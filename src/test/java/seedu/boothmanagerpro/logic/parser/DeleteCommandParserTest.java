package seedu.boothmanagerpro.logic.parser;

import static seedu.boothmanagerpro.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.boothmanagerpro.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.logic.commands.DeleteCommand;
import seedu.boothmanagerpro.model.person.Name;

/**
 * As we are only doing white-box testing, our test cases do not cover path variations
 * outside of the DeleteCommand code. For example, inputs "1" and "1 abc" take the
 * same path through the DeleteCommand, and therefore we test only one of them.
 * The path variation for those two cases occurs inside the ParserUtil, and
 * therefore should be covered by the ParserUtilTest.
 */
public class DeleteCommandParserTest {

    private DeleteCommandParser parser = new DeleteCommandParser();

    @Test
    public void parse_validArgs_returnsDeleteCommand() {
        assertParseSuccess(parser, "1", new DeleteCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, " Alice Pauline ", new DeleteCommand(new Name("Alice Pauline")));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        assertParseFailure(parser, "", String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "0", String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "Alice!",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
    }
}
