package seedu.boothmanagerpro.logic.parser;

import static seedu.boothmanagerpro.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.boothmanagerpro.logic.commands.DeleteCommand;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.person.Name;

/**
 * Parses input arguments and creates a new DeleteCommand object
 */
public class DeleteCommandParser implements Parser<DeleteCommand> {
    private static final String TARGET_USAGE = "delete NAME or delete INDEX";

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteCommand
     * and returns a DeleteCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public DeleteCommand parse(String args) throws ParseException {
        ParserUtil.rejectNamePrefix(args, DeleteCommand.COMMAND_WORD, TARGET_USAGE);
        String trimmedArgs = args.strip();
        if (ParserUtil.isIndexLike(trimmedArgs)) {
            return new DeleteCommand(ParserUtil.parseIndex(trimmedArgs));
        }
        return new DeleteCommand(parseName(trimmedArgs));
    }

    /** Parses a full contact name, showing the delete usage if it is missing or invalid. */
    private Name parseName(String name) throws ParseException {
        try {
            return ParserUtil.parseName(name);
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE), pe);
        }
    }
}
