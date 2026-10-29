package seedu.boothmanagerpro.logic.parser;

import static seedu.boothmanagerpro.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_FOLLOW_UP;
import static seedu.boothmanagerpro.logic.parser.ContactFieldParser.parseValue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.boothmanagerpro.commons.core.LogsCenter;
import seedu.boothmanagerpro.logic.commands.FindCommand;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.person.Company;
import seedu.boothmanagerpro.model.person.ContactMatchesFieldsPredicate;
import seedu.boothmanagerpro.model.person.FollowUpDate;
import seedu.boothmanagerpro.model.person.NameContainsKeywordsPredicate;

/** Parses legacy name keywords or prefixed exact-match contact criteria. */
public class FindCommandParser implements Parser<FindCommand> {
    public static final String MESSAGE_UNSUPPORTED_FIELD =
            "Unsupported search field. Use n/, c/, e/, p/, t/, or f/. Status search is not available yet.";
    public static final String MESSAGE_EMPTY_VALUE = "Search values cannot be empty.";
    public static final String MESSAGE_UNPREFIXED_VALUE =
            "Every exact-match search value must use a supported field prefix.";
    private static final Logger logger = LogsCenter.getLogger(FindCommandParser.class);
    /**
     * Matches only single-letter prefixes (e.g. {@code c/}, {@code f/}) preceded by whitespace or at the
     * start of input. Multi-character tokens such as {@code Design/} are never treated as field delimiters,
     * so slashes embedded in search criteria (e.g. a company name like {@code Acme Design/ Build}) do not
     * cause spurious unsupported-field errors.
     */
    private static final Pattern FIELD_PREFIX = Pattern.compile("(?<!\\S)([a-zA-Z])/");

    @Override
    public FindCommand parse(String args) throws ParseException {
        logger.fine("Parsing contact search.");
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
        Matcher matcher = FIELD_PREFIX.matcher(trimmedArgs);
        if (!matcher.find()) {
            FindCommand command = new FindCommand(
                    new NameContainsKeywordsPredicate(List.of(trimmedArgs.split("\\s+"))));
            logger.fine("Contact search parsed successfully.");
            return command;
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
        assert !criteria.isEmpty() : "Parsed field search must contain at least one criterion";
        FindCommand command = new FindCommand(new ContactMatchesFieldsPredicate(criteria));
        logger.fine("Contact search parsed successfully.");
        return command;
    }

    private void addCriterion(Map<String, Set<String>> criteria, String field, String value) throws ParseException {
        if (value.trim().isEmpty()) {
            throw new ParseException(MESSAGE_EMPTY_VALUE);
        }
        String normalised = ContactMatchesFieldsPredicate.normalise(field, value);
        switch (field) {
            case "n/":
                ParserUtil.parseName(value);
                break;
            case "c/":
                if (!Company.isValidCompany(value)) {
                    throw new ParseException(Company.MESSAGE_CONSTRAINTS);
                }
                break;
            case "e/":
                ParserUtil.parseEmail(value);
                break;
            case "p/":
                ParserUtil.parsePhone(value);
                break;
            case "t/":
                ParserUtil.parseTag(value);
                break;
            case "f/":
                parseValue(PREFIX_FOLLOW_UP, value, FollowUpDate::new);
                break;
            default:
                throw new ParseException(MESSAGE_UNSUPPORTED_FIELD);
        }
        criteria.computeIfAbsent(field, unused -> new HashSet<>()).add(normalised);
    }
}
