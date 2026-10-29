package seedu.boothmanagerpro.logic.parser;

import static seedu.boothmanagerpro.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_COMPANY;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_FOLLOW_UP;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.boothmanagerpro.logic.parser.ContactFieldParser.parseValue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.MatchResult;

import seedu.boothmanagerpro.commons.core.LogsCenter;
import seedu.boothmanagerpro.logic.commands.FindCommand;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.person.Company;
import seedu.boothmanagerpro.model.person.ContactMatchesFieldsPredicate;
import seedu.boothmanagerpro.model.person.Email;
import seedu.boothmanagerpro.model.person.FollowUpDate;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.NameContainsKeywordsPredicate;
import seedu.boothmanagerpro.model.tag.Tag;

/** Parses legacy name keywords or prefixed name and complete-field criteria. */
public class FindCommandParser implements Parser<FindCommand> {
    public static final String MESSAGE_UNSUPPORTED_FIELD =
            "Unsupported search field. Use n/, c/, e/, p/, t/, or f/. Status search is not available yet.";
    public static final String MESSAGE_EMPTY_VALUE = "Search values cannot be empty.";
    public static final String MESSAGE_UNPREFIXED_VALUE =
            "Every exact-match search value must use a supported field prefix.";
    private static final Logger logger = LogsCenter.getLogger(FindCommandParser.class);

    @Override
    public FindCommand parse(String args) throws ParseException {
        logger.fine("Parsing contact search.");
        String trimmedArgs = args.strip();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }
        List<MatchResult> matches = ArgumentTokenizer.findSingleLetterPrefixes(trimmedArgs);
        if (matches.isEmpty()) {
            FindCommand command = new FindCommand(
                    new NameContainsKeywordsPredicate(List.of(trimmedArgs.split("(?U)\\s+"))));
            logger.fine("Contact search parsed successfully.");
            return command;
        }
        if (matches.getFirst().start() != 0) {
            throw new ParseException(MESSAGE_UNPREFIXED_VALUE);
        }

        Map<String, Set<String>> criteria = new HashMap<>();
        for (int i = 0; i < matches.size(); i++) {
            MatchResult current = matches.get(i);
            int end = i + 1 < matches.size() ? matches.get(i + 1).start() : trimmedArgs.length();
            addCriterion(criteria, current.group(1), trimmedArgs.substring(current.end(), end));
        }
        assert !criteria.isEmpty() : "Parsed field search must contain at least one criterion";
        FindCommand command = new FindCommand(new ContactMatchesFieldsPredicate(criteria));
        logger.fine("Contact search parsed successfully.");
        return command;
    }

    private void addCriterion(Map<String, Set<String>> criteria, String field, String value) throws ParseException {
        switch (field) {
            case "n/" -> validateCriterion(PREFIX_NAME, value, Name::new);
            case "c/" -> validateCriterion(PREFIX_COMPANY, value, Company::new);
            case "e/" -> validateCriterion(PREFIX_EMAIL, value, Email::new);
            case "p/" -> validatePhoneCriterion(value);
            case "t/" -> validateCriterion(PREFIX_TAG, value, Tag::new);
            case "f/" -> validateCriterion(PREFIX_FOLLOW_UP, value, FollowUpDate::new);
            default -> throw new ParseException(MESSAGE_UNSUPPORTED_FIELD);
        }
        String normalised = ContactMatchesFieldsPredicate.normalise(field, value);
        criteria.computeIfAbsent(field, unused -> new HashSet<>()).add(normalised);
    }

    private <T> void validateCriterion(Prefix prefix, String value, Function<String, T> constructor)
            throws ParseException {
        requireSearchValue(value);
        parseValue(prefix, value, constructor);
    }

    private void validatePhoneCriterion(String value) throws ParseException {
        requireSearchValue(value);
        ContactFieldParser.parsePhone(value);
    }

    private void requireSearchValue(String value) throws ParseException {
        if (value.isBlank()) {
            throw new ParseException(MESSAGE_EMPTY_VALUE);
        }
    }
}
