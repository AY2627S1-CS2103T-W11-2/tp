package seedu.boothmanagerpro.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.boothmanagerpro.commons.core.index.Index;
import seedu.boothmanagerpro.logic.commands.EditCommand;
import seedu.boothmanagerpro.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.tag.Tag;

/**
 * Parses input arguments and creates a new EditCommand object
 */
public class EditCommandParser implements Parser<EditCommand> {
    public static final String MESSAGE_UNKNOWN_FIELD = "Unknown field prefix: %s. Use n/, p/, e/, a/, or t/.";
    /**
     * Matches only single-letter prefixes (e.g. {@code n/}, {@code p/}) preceded by whitespace or at the
     * start of input. Multi-character tokens such as {@code Design/} are never treated as field delimiters,
     * so slashes embedded in field values do not cause spurious unknown-prefix errors.
     */
    private static final Pattern FIELD_PREFIX = Pattern.compile("(?<!\\S)([a-zA-Z]/)");
    private static final Set<Prefix> ALLOWED_PREFIXES =
            Set.of(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_TAG);

    /**
     * Parses the given {@code String} of arguments in the context of the EditCommand
     * and returns an EditCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public EditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        Matcher fields = FIELD_PREFIX.matcher(args);
        while (fields.find()) {
            Prefix prefix = new Prefix(fields.group(1));
            if (!ALLOWED_PREFIXES.contains(prefix)) {
                throw new ParseException(String.format(MESSAGE_UNKNOWN_FIELD, prefix));
            }
        }
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_TAG);

        Index index;

        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE), pe);
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS);

        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();

        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            String name = argMultimap.getValue(PREFIX_NAME).get();
            ContactFieldParser.requireValue(PREFIX_NAME, name);
            editPersonDescriptor.setName(ParserUtil.parseName(name));
        }
        if (argMultimap.getValue(PREFIX_PHONE).isPresent()) {
            String phone = argMultimap.getValue(PREFIX_PHONE).get();
            ContactFieldParser.requireValue(PREFIX_PHONE, phone);
            editPersonDescriptor.setPhone(ParserUtil.parsePhone(phone));
        }
        if (argMultimap.getValue(PREFIX_EMAIL).isPresent()) {
            String email = argMultimap.getValue(PREFIX_EMAIL).get();
            ContactFieldParser.requireValue(PREFIX_EMAIL, email);
            editPersonDescriptor.setEmail(ParserUtil.parseEmail(email));
        }
        if (argMultimap.getValue(PREFIX_ADDRESS).isPresent()) {
            String address = argMultimap.getValue(PREFIX_ADDRESS).get();
            ContactFieldParser.requireValue(PREFIX_ADDRESS, address);
            editPersonDescriptor.setAddress(ParserUtil.parseAddress(address));
        }
        parseTagsForEdit(argMultimap.getAllValues(PREFIX_TAG)).ifPresent(editPersonDescriptor::setTags);

        if (!editPersonDescriptor.isAnyFieldEdited()) {
            throw new ParseException(EditCommand.MESSAGE_NOT_EDITED);
        }

        return new EditCommand(index, editPersonDescriptor);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>} if {@code tags} is non-empty.
     * If {@code tags} contains only one element which is an empty string, it will be parsed into a
     * {@code Set<Tag>} containing zero tags.
     */
    private Optional<Set<Tag>> parseTagsForEdit(Collection<String> tags) throws ParseException {
        assert tags != null;

        if (tags.isEmpty()) {
            return Optional.empty();
        }
        boolean isClearTags = tags.size() == 1 && tags.iterator().next().isBlank();
        Collection<String> tagSet = isClearTags ? Collections.emptySet() : tags;
        for (String tag : tagSet) {
            ContactFieldParser.requireValue(PREFIX_TAG, tag);
        }
        return Optional.of(ParserUtil.parseTags(tagSet));
    }

}
