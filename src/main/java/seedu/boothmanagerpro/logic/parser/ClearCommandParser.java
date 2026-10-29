package seedu.boothmanagerpro.logic.parser;

import static seedu.boothmanagerpro.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.boothmanagerpro.logic.commands.ClearCommand;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new ClearCommand object.
 */
public class ClearCommandParser implements Parser<ClearCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the ClearCommand
     * and returns a ClearCommand object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    public ClearCommand parse(String args) throws ParseException {
        String trimmed = args.trim();
        if (trimmed.equalsIgnoreCase("confirm")) {
            return new ClearCommand();
        }
        if (trimmed.isEmpty()) {
            throw new ParseException(ClearCommand.MESSAGE_CONFIRMATION);
        }
        throw new ParseException(
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearCommand.MESSAGE_USAGE));
    }
}

