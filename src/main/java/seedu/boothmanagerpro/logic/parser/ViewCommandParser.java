package seedu.boothmanagerpro.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_NAME;

import java.util.List;
import java.util.regex.MatchResult;

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
    public static final String MESSAGE_MISSING_PREFIX =
            "view needs the n/ prefix before the name. Use: view n/NAME, e.g. view n/Alicia Tan";
    public static final String MESSAGE_EMPTY_NAME = "Name cannot be empty. Use: view n/NAME, e.g. view n/Alicia Tan";
    public static final String MESSAGE_INVALID_NAME = Name.MESSAGE_CONSTRAINTS;

    /** Contact fields that view does not accept, which are reported as a missing name when used alone. */
    private static final String OTHER_CONTACT_FIELD = "[cepmstfa]/";

    /**
     * Parses exactly one {@code n/} field containing a name query.
     *
     * @throws ParseException If the name is missing, empty or invalid, or a prefix is missing, repeated or unsupported.
     */
    @Override
    public ViewCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String input = args.strip();
        if (input.isEmpty()) {
            throw new ParseException(MESSAGE_REQUIRED_NAME);
        }
        rejectOtherPrefixes(input);
        ArgumentMultimap fields = ArgumentTokenizer.tokenize(input, PREFIX_NAME);
        if (fields.getAllValues(PREFIX_NAME).size() > 1) {
            throw new ParseException(MESSAGE_DUPLICATE_FIELD);
        }
        if (!fields.getPreamble().isEmpty()) {
            throw new ParseException(MESSAGE_MISSING_PREFIX);
        }
        return new ViewCommand(parseName(fields.getValue(PREFIX_NAME).orElseThrow()));
    }

    /**
     * Rejects prefixes other than {@code n/}. Another contact field on its own means the name is missing; with
     * {@code n/}, or for an unrecognised prefix, it is reported as an unsupported field.
     */
    private void rejectOtherPrefixes(String input) throws ParseException {
        List<String> prefixes = ArgumentTokenizer.findSingleLetterPrefixes(input).stream()
                .map(MatchResult::group).toList();
        boolean hasName = prefixes.contains(PREFIX_NAME.getPrefix());
        boolean hasUnknownField = prefixes.stream()
                .anyMatch(prefix -> !prefix.equals(PREFIX_NAME.getPrefix()) && !prefix.matches(OTHER_CONTACT_FIELD));
        boolean hasOtherContactField = prefixes.stream().anyMatch(prefix -> prefix.matches(OTHER_CONTACT_FIELD));
        if (hasUnknownField || hasOtherContactField && hasName) {
            throw new ParseException(MESSAGE_UNKNOWN_FIELD);
        }
        if (hasOtherContactField) {
            throw new ParseException(MESSAGE_REQUIRED_NAME);
        }
    }

    /**
     * Validates a non-empty name query using the shared {@link Name} rules.
     */
    private Name parseName(String name) throws ParseException {
        if (name.isEmpty()) {
            throw new ParseException(MESSAGE_EMPTY_NAME);
        }
        return ParserUtil.parseName(name);
    }
}
