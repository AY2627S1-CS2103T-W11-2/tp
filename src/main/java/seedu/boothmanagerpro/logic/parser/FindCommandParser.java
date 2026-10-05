package seedu.boothmanagerpro.logic.parser;

import static seedu.boothmanagerpro.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.boothmanagerpro.logic.commands.FindCommand;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.person.ContactMatchesFieldsPredicate;
import seedu.boothmanagerpro.model.person.NameContainsKeywordsPredicate;

/** Parses legacy name keywords or prefixed exact-match contact criteria. */
public class FindCommandParser implements Parser<FindCommand> {
    public static final String MESSAGE_UNSUPPORTED_FIELD =
            "Unsupported search field. Use n/, e/, p/, or t/. Company and status searches are not available yet.";
    public static final String MESSAGE_EMPTY_VALUE = "Search values cannot be empty.";
    public static final String MESSAGE_UNPREFIXED_VALUE =
            "Every exact-match search value must use a supported field prefix.";
    private static final Pattern FIELD_PREFIX = Pattern.compile("(?<!\\S)([^\\s/]+)/");

    @Override
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
        Matcher matcher = FIELD_PREFIX.matcher(trimmedArgs);
        if (!matcher.find()) {
            return new FindCommand(new NameContainsKeywordsPredicate(List.of(trimmedArgs.split("\\s+"))));
        }
        if (matcher.start() != 0) {
            throw new ParseException(MESSAGE_UNPREFIXED_VALUE);
        }

        Map<String, Set<String>> criteria = new HashMap<>();
        String field = matcher.group(1) + "/";
        int valueStart = matcher.end();
        while (matcher.find()) {
            addCriterion(criteria, field, trimmedArgs.substring(valueStart, matcher.start()));
            field = matcher.group(1) + "/";
            valueStart = matcher.end();
        }
        addCriterion(criteria, field, trimmedArgs.substring(valueStart));
        return new FindCommand(new ContactMatchesFieldsPredicate(criteria));
    }

    private void addCriterion(Map<String, Set<String>> criteria, String field, String value) throws ParseException {
        if (!Set.of("n/", "e/", "p/", "t/").contains(field)) {
            throw new ParseException(MESSAGE_UNSUPPORTED_FIELD);
        }
        if (value.trim().isEmpty()) {
            throw new ParseException(MESSAGE_EMPTY_VALUE);
        }
        String normalised = ContactMatchesFieldsPredicate.normalise(field, value);
        switch (field) {
            case "n/":
                ParserUtil.parseName(value);
                break;
            case "e/":
                ParserUtil.parseEmail(value);
                break;
            case "p/":
                ParserUtil.parsePhone(normalised);
                break;
            case "t/":
                ParserUtil.parseTag(value);
                break;
            default:
                throw new ParseException(MESSAGE_UNSUPPORTED_FIELD);
        }
        criteria.computeIfAbsent(field, unused -> new HashSet<>()).add(normalised);
    }
}
