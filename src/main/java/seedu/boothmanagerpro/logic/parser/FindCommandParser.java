package seedu.boothmanagerpro.logic.parser;

import static seedu.boothmanagerpro.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_COMPANY;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_FOLLOW_UP;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_METHOD;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_STATUS;
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
import seedu.boothmanagerpro.model.person.ContactMethod;
import seedu.boothmanagerpro.model.person.Email;
import seedu.boothmanagerpro.model.person.EnquiryStatus;
import seedu.boothmanagerpro.model.person.FollowUpDate;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.NameContainsKeywordsPredicate;
import seedu.boothmanagerpro.model.person.Phone;
import seedu.boothmanagerpro.model.person.RelativeFollowUpFilter;
import seedu.boothmanagerpro.model.tag.Tag;

/** Parses legacy name keywords or prefixed contact search criteria. */
public class FindCommandParser implements Parser<FindCommand> {
    public static final String MESSAGE_UNSUPPORTED_FIELD =
            "Unsupported search field. Use n/, c/, e/, p/, m/, s/, f/, or t/.";
    public static final String MESSAGE_EMPTY_VALUE = "Search values cannot be empty.";
    public static final String MESSAGE_UNPREFIXED_VALUE =
            "Every field search value must use a supported field prefix.";
    public static final String MESSAGE_INVALID_EMAIL_SEARCH =
            "Email search value is invalid. Supply a full or partial email or domain "
            + "(e.g. alicia@technova.com or @technova.com).";
    public static final String MESSAGE_INVALID_PHONE_SEARCH =
            "Phone search value must contain 1 to 15 digits, optionally starting with +, "
            + "and may only include spaces, hyphens, full stops, or parentheses as formatting.";
    private static final String EMAIL_SEARCH_REGEX = "[A-Za-z0-9+_.-]*(?:@[A-Za-z0-9.-]*)?";
    private static final String PHONE_CHARACTERS_REGEX = "\\+?[0-9 ().-]*";
    private static final String PHONE_SEARCH_DIGITS_REGEX = "\\+?[0-9]{1,15}";
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
            case "e/" -> validateEmailCriterion(value);
            case "p/" -> validatePhoneCriterion(value);
            case "m/" -> validateCriterion(PREFIX_METHOD, value, ContactMethod::fromString);
            case "s/" -> validateCriterion(PREFIX_STATUS, value, EnquiryStatus::fromString);
            case "t/" -> validateCriterion(PREFIX_TAG, value, Tag::new);
            case "f/" -> validateFollowUpCriterion(value);
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

    private void validateFollowUpCriterion(String value) throws ParseException {
        requireSearchValue(value);
        if (RelativeFollowUpFilter.fromKeyword(value).isEmpty()) {
            validateCriterion(PREFIX_FOLLOW_UP, value, FollowUpDate::new);
        }
    }

    private void validateEmailCriterion(String value) throws ParseException {
        requireSearchValue(value);
        String trimmed = value.strip();
        if (trimmed.length() > Email.MAX_LENGTH || trimmed.equals("@") || !trimmed.matches(EMAIL_SEARCH_REGEX)) {
            throw new ParseException(String.format(
                    ContactFieldParser.MESSAGE_INVALID_VALUE, PREFIX_EMAIL, MESSAGE_INVALID_EMAIL_SEARCH));
        }
    }

    private void validatePhoneCriterion(String value) throws ParseException {
        requireSearchValue(value);
        String trimmed = value.strip();
        if (trimmed.length() > Phone.MAX_INPUT_LENGTH || !trimmed.matches(PHONE_CHARACTERS_REGEX)
                || !Phone.normalize(trimmed).matches(PHONE_SEARCH_DIGITS_REGEX)) {
            throw new ParseException(String.format(
                    ContactFieldParser.MESSAGE_INVALID_VALUE, PREFIX_PHONE, MESSAGE_INVALID_PHONE_SEARCH));
        }
    }

    private void requireSearchValue(String value) throws ParseException {
        if (value.isBlank()) {
            throw new ParseException(MESSAGE_EMPTY_VALUE);
        }
    }
}
