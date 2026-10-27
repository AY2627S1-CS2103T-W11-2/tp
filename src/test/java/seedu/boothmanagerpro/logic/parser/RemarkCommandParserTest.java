package seedu.boothmanagerpro.logic.parser;

import static seedu.boothmanagerpro.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_REMARK;
import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.boothmanagerpro.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.boothmanagerpro.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.boothmanagerpro.logic.Messages;
import seedu.boothmanagerpro.logic.commands.RemarkCommand;
import seedu.boothmanagerpro.model.person.Remark;

public class RemarkCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE);

    private final RemarkCommandParser parser = new RemarkCommandParser();
    private final String nonEmptyRemark = "Some remark.";

    @Test
    public void parse_indexSpecified_success() {
        // have remark
        String userInput = INDEX_FIRST_PERSON.getOneBased() + " " + PREFIX_REMARK + nonEmptyRemark;
        RemarkCommand expectedCommand = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(nonEmptyRemark));
        assertParseSuccess(parser, userInput, expectedCommand);

        // no remark
        userInput = INDEX_FIRST_PERSON.getOneBased() + " " + PREFIX_REMARK;
        expectedCommand = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(""));
        assertParseSuccess(parser, userInput, expectedCommand);

        // no r/ at all also removes the remark
        assertParseSuccess(parser, String.valueOf(INDEX_FIRST_PERSON.getOneBased()), expectedCommand);
    }

    @Test
    public void parse_missingOrInvalidIndex_failure() {
        // no index
        assertParseFailure(parser, PREFIX_REMARK + nonEmptyRemark, MESSAGE_INVALID_FORMAT);

        // no parameters
        assertParseFailure(parser, RemarkCommand.COMMAND_WORD, MESSAGE_INVALID_FORMAT);

        // invalid index
        assertParseFailure(parser, "0 " + PREFIX_REMARK + nonEmptyRemark, MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "a " + PREFIX_REMARK + nonEmptyRemark, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_repeatedRemark_failure() {
        String userInput = "1 " + PREFIX_REMARK + "first " + PREFIX_REMARK + "second";
        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(PREFIX_REMARK));
    }
}
