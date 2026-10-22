package seedu.boothmanagerpro.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_ORDER;

import seedu.boothmanagerpro.logic.commands.ListCommand;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.person.SortField;

/**
 * Parses a list request, which either takes no parameters or a single {@code o/FIELD} sort order.
 */
public class ListCommandParser implements Parser<ListCommand> {

    /**
     * Parses {@code args} into a list command in stored order, or sorted by the requested field.
     *
     * @throws ParseException If there is text outside {@code o/}, {@code o/} is repeated or the field is unknown.
     */
    @Override
    public ListCommand parse(String args) throws ParseException {
        requireNonNull(args);
        if (args.isBlank()) {
            return new ListCommand();
        }
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, PREFIX_ORDER);
        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ListCommand.MESSAGE_USAGE));
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_ORDER);
        // Non-blank input with an empty preamble must start with o/, so the value is always present here.
        String order = argMultimap.getValue(PREFIX_ORDER).orElseThrow();
        SortField field = SortField.fromKeyword(order).orElseThrow(() ->
                new ParseException(SortField.MESSAGE_CONSTRAINTS + "\n" + ListCommand.MESSAGE_USAGE));
        return new ListCommand(field);
    }
}
