package seedu.boothmanagerpro.logic.parser;

import static seedu.boothmanagerpro.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.List;
import java.util.regex.MatchResult;

import seedu.boothmanagerpro.logic.commands.DeleteCommand;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new DeleteCommand object
 */
public class DeleteCommandParser implements Parser<DeleteCommand> {
    public static final String MESSAGE_UNKNOWN_FIELD =
            "Unexpected field prefix: %s. Use: delete NAME or delete INDEX";
    private static final String TARGET_USAGE = "delete NAME or delete INDEX";

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteCommand
     * and returns a DeleteCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public DeleteCommand parse(String args) throws ParseException {
        ParserUtil.rejectNamePrefix(args, DeleteCommand.COMMAND_WORD, TARGET_USAGE);
        String trimmedArgs = args.strip();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteCommand.MESSAGE_USAGE));
        }
        List<MatchResult> prefixes = ArgumentTokenizer.findSingleLetterPrefixes(trimmedArgs);
        if (!prefixes.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_UNKNOWN_FIELD, prefixes.getFirst().group(1)));
        }
        if (ParserUtil.isIndexLike(trimmedArgs)) {
            return new DeleteCommand(ParserUtil.parseIndex(trimmedArgs));
        }
        ParserUtil.rejectMultipleIndices(trimmedArgs);
        return new DeleteCommand(ParserUtil.parseName(trimmedArgs));
    }
}
