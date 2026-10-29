package seedu.boothmanagerpro.logic.parser;

import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.boothmanagerpro.logic.parser.ContactFieldParser.parseValue;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import seedu.boothmanagerpro.logic.commands.TagCommand;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.tag.Tag;

/** Parses a contact target and one or more t/TAG fields to toggle. */
public class TagCommandParser implements Parser<TagCommand> {
    public static final String MESSAGE_UNKNOWN_FIELD = "Unknown field prefix: %s. Use t/.";
    /**
     * Matches only single-letter prefixes (e.g. {@code t/}) preceded by whitespace or at the
     * start of input. Multi-character tokens are never treated as field delimiters.
     */
    private static final Pattern FIELD_PREFIX = Pattern.compile("(?<!\\S)([a-zA-Z]/)");

    @Override
    public TagCommand parse(String args) throws ParseException {
        ArgumentMultimap fields = tokenize(args);
        validateStructure(fields);
        Set<Tag> tags = parseTags(fields);
        String target = fields.getPreamble();
        return target.matches("[0-9]+")
                ? new TagCommand(ParserUtil.parseIndex(target), tags)
                : new TagCommand(ParserUtil.parseName(target), tags);
    }

    /** Rejects unsupported prefix-like tokens before tokenising t/TAG fields and the target. */
    private ArgumentMultimap tokenize(String args) throws ParseException {
        for (MatchResult match : FIELD_PREFIX.matcher(args).results().toList()) {
            if (!PREFIX_TAG.equals(new Prefix(match.group(1)))) {
                throw new ParseException(String.format(MESSAGE_UNKNOWN_FIELD, match.group(1)));
            }
        }
        return ArgumentTokenizer.tokenize(args, PREFIX_TAG);
    }

    /** Requires both a contact target and at least one t/TAG field. */
    private void validateStructure(ArgumentMultimap fields) throws ParseException {
        if (fields.getPreamble().isBlank()) {
            throw new ParseException("Specify a contact name or displayed index.\n" + TagCommand.MESSAGE_USAGE);
        }
        if (fields.getValue(PREFIX_TAG).isEmpty()) {
            throw new ParseException("Supply at least one t/TAG field.\n" + TagCommand.MESSAGE_USAGE);
        }
    }

    /** Deduplicates repeated tags in input order and rejects blank or invalid tag values. */
    private Set<Tag> parseTags(ArgumentMultimap fields) throws ParseException {
        Set<Tag> tags = new LinkedHashSet<>();
        for (String value : fields.getAllValues(PREFIX_TAG)) {
            tags.add(parseValue(PREFIX_TAG, value, Tag::new));
        }
        return tags;
    }
}
