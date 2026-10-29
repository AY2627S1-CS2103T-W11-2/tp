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
import seedu.boothmanagerpro.logic.commands.AddCommand;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.person.Company;
import seedu.boothmanagerpro.model.person.ContactMethod;
import seedu.boothmanagerpro.model.person.Email;
import seedu.boothmanagerpro.model.person.EnquiryStatus;
import seedu.boothmanagerpro.model.person.FollowUpDate;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.model.person.Phone;
import seedu.boothmanagerpro.model.tag.Tag;

/**
 * Parses exhibitor additions with required name, company, email and phone fields.
 * Only single-letter prefixes (e.g. {@code n/}, {@code c/}) are treated as field delimiters, so slashes
 * embedded in field values (e.g. a company name like {@code Acme Design/ Build}) are accepted without error.
 * Single-letter tokens that do not match a supported prefix are still reported as unknown fields.
 */
public class AddCommandParser implements Parser<AddCommand> {
    public static final String MESSAGE_MISSING_FIELD = "Missing required fields: %s. Supply a value after each prefix.";
    public static final String MESSAGE_REPEATED_FIELD =
            "Repeated fields: %s. Keep one value per field; only t/ may repeat.";
    public static final String MESSAGE_UNKNOWN_FIELD =
            "Unknown field prefix: %s. Use n/, c/, e/, p/, m/, s/, f/, or t/.";
    public static final String MESSAGE_PREAMBLE = "Unexpected text before the first field. "
            + "Start with a prefix, for example: add n/Alicia Tan c/TechNova e/alicia@example.com p/91234567";
    private static final Logger logger = LogsCenter.getLogger(AddCommandParser.class);
    /**
     * Matches only single-letter prefixes (e.g. {@code n/}, {@code c/}) preceded by whitespace or at the
     * start of input. This deliberately excludes multi-character tokens such as {@code Design/} so that
     * slash characters embedded in field values (e.g. a company name like {@code Acme Design/ Build})
     * are never mistaken for field delimiters.
     */
    private static final Pattern FIELD_PREFIX = Pattern.compile("(?<!\\S)([a-zA-Z]/)");
    private static final Set<Prefix> ALLOWED_PREFIXES = Set.of(PREFIX_NAME, PREFIX_COMPANY, PREFIX_EMAIL,
            PREFIX_PHONE, PREFIX_METHOD, PREFIX_STATUS, PREFIX_FOLLOW_UP, PREFIX_TAG);
    private static final List<Prefix> SINGLE_VALUE_PREFIXES = List.of(PREFIX_NAME, PREFIX_COMPANY,
            PREFIX_EMAIL, PREFIX_PHONE, PREFIX_METHOD, PREFIX_STATUS, PREFIX_FOLLOW_UP);
    private static final List<Prefix> REQUIRED_PREFIXES = List.of(PREFIX_NAME, PREFIX_COMPANY,
            PREFIX_EMAIL, PREFIX_PHONE);

    /**
     * Validates syntax and field values without modifying the model.
     * Structural errors are checked before values: unknown tokens, repeated fields, then missing prefixes.
     * Repeated tags are allowed; an omitted method remains unspecified.
     *
     * @param args Arguments following the command word, with optional leading whitespace.
     * @return A command containing a validated, normalised contact.
     * @throws ParseException If the syntax or any field violates the add specification.
     */
    @Override
    public AddCommand parse(String args) throws ParseException {
        logger.finer("Parsing AddCommand arguments: " + args);
        ArgumentMultimap fields = tokenize(args);
        validateStructure(fields);
        Person person = parseContact(fields);
        logger.fine("Successfully parsed AddCommand for: " + AddCommand.formatIdentity(person));
        return new AddCommand(person);
    }

    /** Checks field cardinality before attempting value conversion, preserving error precedence. */
    private void validateStructure(ArgumentMultimap fields) throws ParseException {
        List<Prefix> repeated = SINGLE_VALUE_PREFIXES.stream()
                .filter(prefix -> fields.getAllValues(prefix).size() > 1).toList();
        if (!repeated.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_REPEATED_FIELD, formatPrefixes(repeated)));
        }
        List<Prefix> missing = REQUIRED_PREFIXES.stream()
                .filter(prefix -> fields.getValue(prefix).isEmpty()).toList();
        if (!missing.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_MISSING_FIELD, formatPrefixes(missing)));
        }
    }

    /** Formats every affected prefix in the stable order defined by the command specification. */
    private String formatPrefixes(List<Prefix> prefixes) {
        return prefixes.stream().map(Prefix::toString).collect(Collectors.joining(", "));
    }

    /** Converts fields in command-help order using the same value objects as JSON loading. */
    private Person parseContact(ArgumentMultimap fields) throws ParseException {
        Name name = parseValue(PREFIX_NAME, fields.getValue(PREFIX_NAME).orElseThrow(), Name::new);
        Company company = parseValue(PREFIX_COMPANY, fields.getValue(PREFIX_COMPANY).orElseThrow(), Company::new);
        Email email = parseValue(PREFIX_EMAIL, fields.getValue(PREFIX_EMAIL).orElseThrow(), Email::new);
        Phone phone = parseValue(PREFIX_PHONE, fields.getValue(PREFIX_PHONE).orElseThrow(), Phone::new);
        Optional<ContactMethod> method = Optional.empty();
        if (fields.getValue(PREFIX_METHOD).isPresent()) {
            method = Optional.of(parseValue(PREFIX_METHOD, fields.getValue(PREFIX_METHOD).orElseThrow(),
                    ContactMethod::fromString));
        }
        EnquiryStatus status = parseOptional(fields, PREFIX_STATUS, EnquiryStatus::fromString)
                .orElse(EnquiryStatus.NEEDS_FOLLOW_UP);
        Optional<FollowUpDate> date = parseOptional(fields, PREFIX_FOLLOW_UP, FollowUpDate::new);
        Set<Tag> tags = new LinkedHashSet<>();
        for (String value : fields.getAllValues(PREFIX_TAG)) {
            tags.add(parseValue(PREFIX_TAG, value, Tag::new));
        }
        return Person.createExhibitor(name, company, email, phone, method, tags, status, date);
    }

    /** Parses an optional field only when supplied; explicit blank values still produce an error. */
    private <T> Optional<T> parseOptional(ArgumentMultimap fields, Prefix prefix, Function<String, T> constructor)
            throws ParseException {
        if (fields.getValue(prefix).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(parseValue(prefix, fields.getValue(prefix).orElseThrow(), constructor));
    }

    /**
     * Splits fields at whitespace-delimited single-letter prefix tokens (e.g. {@code n/}, {@code c/}),
     * preserving repeated values and their order. Words between prefixes belong to the preceding value,
     * allowing multi-word names and company names that contain slash characters (e.g. {@code Acme Design/ Build}).
     * Multi-character tokens such as {@code Design/} are never treated as delimiters.
     *
     * @throws ParseException If text precedes the first prefix or a single-letter prefix is not supported.
     */
    private ArgumentMultimap tokenize(String args) throws ParseException {
        List<MatchResult> matches = FIELD_PREFIX.matcher(args).results().toList();
        if (matches.isEmpty()) {
            if (!args.isBlank()) {
                throw new ParseException(MESSAGE_PREAMBLE);
            }
            return new ArgumentMultimap();
        }
        if (!args.substring(0, matches.getFirst().start()).isBlank()) {
            throw new ParseException(MESSAGE_PREAMBLE);
        }
        ArgumentMultimap fields = new ArgumentMultimap();
        for (int i = 0; i < matches.size(); i++) {
            MatchResult match = matches.get(i);
            Prefix prefix = new Prefix(match.group(1));
            if (!ALLOWED_PREFIXES.contains(prefix)) {
                throw new ParseException(String.format(MESSAGE_UNKNOWN_FIELD, prefix));
            }
            int end = i + 1 < matches.size() ? matches.get(i + 1).start() : args.length();
            fields.put(prefix, args.substring(match.end(), end).trim());
        }
        return fields;
    }
}
