package seedu.boothmanagerpro.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_NAME;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.boothmanagerpro.logic.commands.ViewCommand;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.person.Name;

/**
 * Parses a view request with exactly one prefixed name query.
 */
public class ViewCommandParser implements Parser<ViewCommand> {
    public static final String MESSAGE_REQUIRED_NAME = "Use required field: n/NAME.";
    public static final String MESSAGE_DUPLICATE_FIELD = "Each field can only be specified once.";
    public static final String MESSAGE_UNKNOWN_FIELD = "Unknown field prefix. Use n/.";
    public static final String MESSAGE_INVALID_NAME = "Names must be 1 to 80 characters "
            + "and contain at least one letter. "
            + "Names may include letters, spaces, hyphens, apostrophes, or full stops.";

    private static final Pattern FIELD = Pattern.compile("(?:^|\\s)([^\\s/]+/)");
    private static final Pattern VALID_NAME = Pattern.compile("(?=.*\\p{L})[\\p{L}\\p{M} .’'\\-]{1,80}");

    /**
     * Parses exactly one {@code n/} field containing a name query.
     *
     * @throws ParseException If the name is missing or invalid, or a prefix is repeated or unsupported.
     */
    @Override
    public ViewCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String input = args.strip();
        if (input.isEmpty()) {
            throw new ParseException(MESSAGE_REQUIRED_NAME);
        }
        Matcher fields = FIELD.matcher(input);
        int nameFields = 0;
        while (fields.find()) {
            String prefix = fields.group(1);
            if (!prefix.equals(PREFIX_NAME.getPrefix())) {
                if (prefix.matches("[cepmt]/")) {
                    throw new ParseException(MESSAGE_REQUIRED_NAME);
                }
                throw new ParseException(MESSAGE_UNKNOWN_FIELD);
            }
            nameFields++;
        }
        if (nameFields > 1) {
            throw new ParseException(MESSAGE_DUPLICATE_FIELD);
        }
        if (!input.startsWith(PREFIX_NAME.getPrefix()) || nameFields == 0) {
            throw new ParseException(MESSAGE_UNKNOWN_FIELD);
        }
        String name = input.substring(PREFIX_NAME.getPrefix().length()).strip();
        if (name.contains("/")) {
            throw new ParseException(MESSAGE_UNKNOWN_FIELD);
        }
        if (!VALID_NAME.matcher(name).matches()) {
            throw new ParseException(MESSAGE_INVALID_NAME);
        }
        return new ViewCommand(new Name(name));
    }
}
