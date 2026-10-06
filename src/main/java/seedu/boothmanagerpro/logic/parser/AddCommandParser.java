package seedu.boothmanagerpro.logic.parser;

import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_COMPANY;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_METHOD;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import seedu.boothmanagerpro.logic.commands.AddCommand;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.person.Company;
import seedu.boothmanagerpro.model.person.ContactMethod;
import seedu.boothmanagerpro.model.person.Email;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.model.person.Phone;
import seedu.boothmanagerpro.model.tag.Tag;

/**
 * Parses exhibitor additions with required name, company, email and phone fields.
 * Unlike the shared tokenizer, this parser detects unknown prefix-like tokens instead of treating them as values.
 * Other commands retain their existing syntax.
 */
public class AddCommandParser implements Parser<AddCommand> {
    public static final String MESSAGE_MISSING_FIELD = "Missing required field: NAME, COMPANY, EMAIL, or PHONE.";
    public static final String MESSAGE_REPEATED_FIELD = "Each field can only be specified once.";
    public static final String MESSAGE_UNKNOWN_FIELD = "Unknown field prefix. Use n/, c/, e/, p/, m/, or t/.";
    private static final Pattern FIELD_PREFIX = Pattern.compile("(?<!\\S)([^\\s/]+/)");
    private static final Set<Prefix> ALLOWED_PREFIXES = Set.of(PREFIX_NAME, PREFIX_COMPANY, PREFIX_EMAIL,
            PREFIX_PHONE, PREFIX_METHOD, PREFIX_TAG);
    private static final List<Prefix> SINGLE_VALUE_PREFIXES = List.of(PREFIX_NAME, PREFIX_COMPANY,
            PREFIX_EMAIL, PREFIX_PHONE, PREFIX_METHOD);
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
        ArgumentMultimap fields = tokenize(args);
        validateStructure(fields);
        return new AddCommand(parseContact(fields));
    }

    /** Checks field cardinality before attempting value conversion, preserving error precedence. */
    private void validateStructure(ArgumentMultimap fields) throws ParseException {
        for (Prefix prefix : SINGLE_VALUE_PREFIXES) {
            if (fields.getAllValues(prefix).size() > 1) {
                throw new ParseException(MESSAGE_REPEATED_FIELD);
            }
        }
        for (Prefix prefix : REQUIRED_PREFIXES) {
            if (fields.getValue(prefix).isEmpty()) {
                throw new ParseException(MESSAGE_MISSING_FIELD);
            }
        }
    }

    /** Converts validated field structure into model values; value objects own their constraints. */
    private Person parseContact(ArgumentMultimap fields) throws ParseException {
        Name name = ParserUtil.parseName(fields.getValue(PREFIX_NAME).orElseThrow());
        Email email = ParserUtil.parseEmail(fields.getValue(PREFIX_EMAIL).orElseThrow());
        Phone phone = ParserUtil.parsePhone(fields.getValue(PREFIX_PHONE).orElseThrow());
        Set<Tag> tags = ParserUtil.parseTags(fields.getAllValues(PREFIX_TAG));
        try {
            Company company = new Company(fields.getValue(PREFIX_COMPANY).orElseThrow());
            Optional<ContactMethod> method = fields.getValue(PREFIX_METHOD).map(ContactMethod::fromString);
            return Person.createExhibitor(name, company, email, phone, method, tags);
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }

    /**
     * Splits fields at whitespace-delimited prefix-like tokens, preserving repeated values and their order.
     * Words between prefixes belong to the preceding value, allowing multi-word names and companies.
     *
     * @throws ParseException If text precedes the first prefix or a prefix is not supported.
     */
    private ArgumentMultimap tokenize(String args) throws ParseException {
        List<MatchResult> matches = FIELD_PREFIX.matcher(args).results().toList();
        if (matches.isEmpty()) {
            if (!args.isBlank()) {
                throw new ParseException(MESSAGE_UNKNOWN_FIELD);
            }
            return new ArgumentMultimap();
        }
        if (!args.substring(0, matches.getFirst().start()).isBlank()) {
            throw new ParseException(MESSAGE_UNKNOWN_FIELD);
        }
        ArgumentMultimap fields = new ArgumentMultimap();
        for (int i = 0; i < matches.size(); i++) {
            MatchResult match = matches.get(i);
            Prefix prefix = new Prefix(match.group(1));
            if (!ALLOWED_PREFIXES.contains(prefix)) {
                throw new ParseException(MESSAGE_UNKNOWN_FIELD);
            }
            int end = i + 1 < matches.size() ? matches.get(i + 1).start() : args.length();
            fields.put(prefix, args.substring(match.end(), end).trim());
        }
        return fields;
    }
}
