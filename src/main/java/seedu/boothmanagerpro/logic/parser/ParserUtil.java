package seedu.boothmanagerpro.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import seedu.boothmanagerpro.commons.core.index.Index;
import seedu.boothmanagerpro.commons.util.StringUtil;
import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.person.Address;
import seedu.boothmanagerpro.model.person.Email;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.Phone;
import seedu.boothmanagerpro.model.tag.Tag;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";
    public static final String MESSAGE_INDEX_TOO_LARGE =
            "Index is too large. Use a number shown in the current list, find or view results.";
    public static final String MESSAGE_NAME_PREFIX_NOT_ALLOWED = "Don't include n/ for %1$s. Use: %2$s";
    public static final String MESSAGE_PHONE_CHARACTERS =
            "Phone number can only contain digits, spaces, hyphens, and a + at the start.";
    public static final String MESSAGE_PHONE_DIGITS =
            "Phone number must contain 7 to 15 digits, optionally starting with +.";

    private static final String INDEX_LIKE_REGEX = "[+-]?[0-9]+";
    private static final String PHONE_CHARACTERS_REGEX = "\\+?[0-9 -]*";

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the index is not a positive integer, or is too large to be a list position.
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.strip();
        if (StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            return Index.fromOneBased(Integer.parseInt(trimmedIndex));
        }
        if (isTooLargeIndex(trimmedIndex)) {
            throw new ParseException(MESSAGE_INDEX_TOO_LARGE);
        }
        throw new ParseException(MESSAGE_INVALID_INDEX);
    }

    /** Returns true if {@code target} is written as a whole number, including zero or a negative number. */
    public static boolean isIndexLike(String target) {
        return target.strip().matches(INDEX_LIKE_REGEX);
    }

    /** Returns true if {@code index} has only digits but exceeds the largest supported index. */
    private static boolean isTooLargeIndex(String index) {
        return index.matches("[0-9]+") && !index.matches("0*") && !StringUtil.isNonZeroUnsignedInteger(index);
    }

    /**
     * Rejects arguments that start with {@code n/}, which {@code commandWord} does not use before its target.
     *
     * @param usage The correct format to show, e.g. {@code delete NAME or delete INDEX}.
     * @throws ParseException If {@code args} starts with {@code n/}.
     */
    public static void rejectNamePrefix(String args, String commandWord, String usage) throws ParseException {
        if (args.strip().startsWith(CliSyntax.PREFIX_NAME.getPrefix())) {
            throw new ParseException(String.format(MESSAGE_NAME_PREFIX_NOT_ALLOWED, commandWord, usage));
        }
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.strip();
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.strip();
        if (!trimmedPhone.matches(PHONE_CHARACTERS_REGEX)) {
            throw new ParseException(MESSAGE_PHONE_CHARACTERS);
        }
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(MESSAGE_PHONE_DIGITS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String address} into an {@code Address}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code address} is invalid.
     */
    public static Address parseAddress(String address) throws ParseException {
        requireNonNull(address);
        String trimmedAddress = address.strip();
        if (!Address.isValidAddress(trimmedAddress)) {
            throw new ParseException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(trimmedAddress);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.strip();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = tag.strip();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     * Retains first-occurrence order and ignores repeated identical trimmed tags.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new LinkedHashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }
}
