package seedu.boothmanagerpro.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.commons.util.AppUtil.checkArgument;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a Person's phone number(s) in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {

    public static final String MESSAGE_CONSTRAINTS =
            "Each phone number must contain 7 to 15 digits (excluding any extension), optionally starting with +.";
    // Maximum realistic input length, allowing several formatted numbers with extensions
    public static final int MAX_INPUT_LENGTH = 100;

    /** Splits multiple numbers written as {@code 9123 4567, 8765 4321}, with {@code ,}, {@code ;} or {@code /}. */
    private static final String SEPARATOR_REGEX = "\\s*[,;/]\\s*";
    /** A number with an optional leading plus, formatting characters, and an optional extension. */
    private static final Pattern NUMBER_PATTERN = Pattern.compile(
            "(\\+?[0-9(][0-9 ().-]*?)(?:\\s*(?i:ext\\.?|x)\\s*[0-9]{1,6})?");
    /** Possessive so that malformed input fails in linear time. */
    private static final String ALLOWED_CHARACTERS_REGEX = "(?i)(?:[0-9 +().,;/-]|ext|x)*+";

    public final String value;

    /**
     * Validates one or more phone numbers and stores them as typed, with surrounding whitespace removed and
     * consecutive spaces collapsed, so the user's own spacing, hyphens and parentheses are displayed.
     *
     * @param phone A valid phone number.
     */
    public Phone(String phone) {
        requireNonNull(phone);
        checkArgument(isValidPhone(phone), MESSAGE_CONSTRAINTS);
        value = phone.strip().replaceAll(" {2,}", " ");
    }

    /**
     * Returns true if a given string is one or more valid phone numbers. Each number may contain digits, spaces,
     * hyphens, full stops and parentheses, may start with {@code +}, and may end with an extension such as
     * {@code ext 88} or {@code x88}. Each number must contain 7 to 15 digits, excluding the extension.
     */
    public static boolean isValidPhone(String test) {
        requireNonNull(test);
        String trimmed = test.strip();
        if (trimmed.isEmpty() || trimmed.length() > MAX_INPUT_LENGTH) {
            return false;
        }
        for (String number : trimmed.split(SEPARATOR_REGEX, -1)) {
            Matcher matcher = NUMBER_PATTERN.matcher(number);
            if (!matcher.matches()) {
                return false;
            }
            long digits = matcher.group(1).chars().filter(Character::isDigit).count();
            if (digits < 7 || digits > 15) {
                return false;
            }
        }
        return true;
    }

    /** Returns true if {@code test} uses only characters allowed in phone numbers, extensions and separators. */
    public static boolean hasAllowedCharacters(String test) {
        requireNonNull(test);
        return test.strip().matches(ALLOWED_CHARACTERS_REGEX);
    }

    /** Returns a comparison form with spaces, hyphens, full stops and parentheses removed. */
    public static String normalize(String rawPhone) {
        return rawPhone.strip().replaceAll("[\\s().-]", "");
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Phone otherPhone)) {
            return false;
        }

        return value.equals(otherPhone.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
