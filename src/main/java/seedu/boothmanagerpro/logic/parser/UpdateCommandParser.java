package seedu.boothmanagerpro.logic.parser;

import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_COMPANY;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_FOLLOW_UP;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_METHOD;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.boothmanagerpro.logic.parser.ContactFieldParser.parseValue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import seedu.boothmanagerpro.commons.core.LogsCenter;
import seedu.boothmanagerpro.logic.commands.UpdateCommand;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.person.Company;
import seedu.boothmanagerpro.model.person.ContactMethod;
import seedu.boothmanagerpro.model.person.ContactUpdate;
import seedu.boothmanagerpro.model.person.Email;
import seedu.boothmanagerpro.model.person.EnquiryStatus;
import seedu.boothmanagerpro.model.person.FollowUpDate;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.Phone;
import seedu.boothmanagerpro.model.tag.Tag;

/** Parses a target followed by validated partial changes, without changing model state. */
public class UpdateCommandParser implements Parser<UpdateCommand> {
    private static final Logger logger = LogsCenter.getLogger(UpdateCommandParser.class);
    /**
     * Matches only single-letter prefixes (e.g. {@code c/}, {@code s/}) preceded by whitespace or at the
     * start of input. Multi-character tokens such as {@code Design/} are never treated as field delimiters,
     * so slashes embedded in field values (e.g. a company name like {@code Acme Design/ Build}) do not
     * cause spurious unknown-prefix errors.
     */
    private static final Pattern FIELD_PREFIX = Pattern.compile("(?<!\\S)([a-zA-Z]/)");
    private static final List<Prefix> PREFIXES = List.of(PREFIX_NAME, PREFIX_COMPANY, PREFIX_EMAIL,
            PREFIX_PHONE, PREFIX_METHOD, PREFIX_STATUS, PREFIX_FOLLOW_UP, PREFIX_TAG);

    @Override
    public UpdateCommand parse(String args) throws ParseException {
        logger.finer("Parsing UpdateCommand arguments: " + args);
        ArgumentMultimap fields = tokenize(args);
        validateStructure(fields);
        ContactUpdate changes = parseChanges(fields);
        String target = fields.getPreamble();
        UpdateCommand command = target.matches("[0-9]+")
                ? new UpdateCommand(ParserUtil.parseIndex(target), changes)
                : new UpdateCommand(ParserUtil.parseName(target), changes);
        logger.fine("Successfully parsed UpdateCommand targeting: " + target);
        return command;
    }

    /** Rejects unsupported single-letter prefix tokens before tokenising recognised fields and the target. */
    private ArgumentMultimap tokenize(String args) throws ParseException {
        for (MatchResult match : FIELD_PREFIX.matcher(args).results().toList()) {
            if (!PREFIXES.contains(new Prefix(match.group(1)))) {
                throw new ParseException(String.format(AddCommandParser.MESSAGE_UNKNOWN_FIELD, match.group(1)));
            }
        }
        return ArgumentTokenizer.tokenize(args, PREFIXES.toArray(Prefix[]::new));
    }

    /** Requires a target and a change; only tag fields may repeat. */
    private void validateStructure(ArgumentMultimap fields) throws ParseException {
        if (fields.getPreamble().isBlank()) {
            throw new ParseException("Specify a contact name or displayed index.\n" + UpdateCommand.MESSAGE_USAGE);
        }
        if (PREFIXES.stream().noneMatch(prefix -> fields.getValue(prefix).isPresent())) {
            throw new ParseException("Supply at least one field to update.\n" + UpdateCommand.MESSAGE_USAGE);
        }
        String repeated = PREFIXES.stream().filter(prefix -> !prefix.equals(PREFIX_TAG))
                .filter(prefix -> fields.getAllValues(prefix).size() > 1)
                .map(Prefix::toString).collect(Collectors.joining(", "));
        if (!repeated.isEmpty()) {
            throw new ParseException(String.format(AddCommandParser.MESSAGE_REPEATED_FIELD, repeated));
        }
    }

    /** Uses the same domain constructors and field errors as add, with explicit date/tag clearing. */
    private ContactUpdate parseChanges(ArgumentMultimap fields) throws ParseException {
        return new ContactUpdate(optional(fields, PREFIX_NAME, Name::new),
                optional(fields, PREFIX_COMPANY, Company::new), optional(fields, PREFIX_EMAIL, Email::new),
                optional(fields, PREFIX_PHONE, Phone::new), optional(fields, PREFIX_METHOD, ContactMethod::fromString),
                optional(fields, PREFIX_STATUS, EnquiryStatus::fromString), parseDate(fields), parseTags(fields));
    }

    private <T> Optional<T> optional(ArgumentMultimap fields, Prefix prefix, Function<String, T> constructor)
            throws ParseException {
        if (fields.getValue(prefix).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(parseValue(prefix, fields.getValue(prefix).orElseThrow(), constructor));
    }

    /** Distinguishes an omitted date (preserve), blank date (clear), and supplied date (replace). */
    private Optional<Optional<FollowUpDate>> parseDate(ArgumentMultimap fields) throws ParseException {
        Optional<String> value = fields.getValue(PREFIX_FOLLOW_UP);
        if (value.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(value.orElseThrow().isBlank() ? Optional.empty()
                : Optional.of(parseValue(PREFIX_FOLLOW_UP, value.orElseThrow(), FollowUpDate::new)));
    }

    /** Replaces tags in input order; a sole blank tag clears, but mixed blank/nonblank tags are errors. */
    private Optional<Set<Tag>> parseTags(ArgumentMultimap fields) throws ParseException {
        List<String> values = fields.getAllValues(PREFIX_TAG);
        if (values.isEmpty()) {
            return Optional.empty();
        }
        Set<Tag> tags = new LinkedHashSet<>();
        if (values.size() == 1 && values.getFirst().isBlank()) {
            return Optional.of(tags);
        }
        for (String value : values) {
            tags.add(parseValue(PREFIX_TAG, value, Tag::new));
        }
        return Optional.of(tags);
    }
}
