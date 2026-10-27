package seedu.boothmanagerpro.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_REMARK;

import seedu.boothmanagerpro.commons.core.index.Index;
import seedu.boothmanagerpro.logic.commands.RemarkCommand;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.person.Remark;

/**
 * Parses input arguments and creates a new {@code RemarkCommand} object.
 */
public class RemarkCommandParser implements Parser<RemarkCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the {@code RemarkCommand}
     * and returns a {@code RemarkCommand} object for execution. A missing or empty {@code r/} removes the remark.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    @Override
    public RemarkCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_REMARK);

        Index index;
        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE), pe);
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_REMARK);
        String remark = argMultimap.getValue(PREFIX_REMARK).orElse("");

        return new RemarkCommand(index, new Remark(remark));
    }
}
