package seedu.boothmanagerpro.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.commons.util.AppUtil.checkArgument;

import java.util.Locale;

/**
 * Represents a Person's email in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidEmail(String)}
 */
public class Email {

    public static final String MESSAGE_CONSTRAINTS = "Email address is invalid.";
    public static final int MAX_LENGTH = 254; // RFC 5321 standard maximum email length
    public static final int MAX_LOCAL_PART_LENGTH = 64; // RFC 5321 standard maximum local-part length
    public static final int MAX_DOMAIN_PART_LENGTH = 255; // RFC 5321 standard maximum domain length

    private static final String SPECIAL_CHARACTERS = "+_.-";
    // single alphanumeric character except underscore
    private static final String ALPHANUMERIC_NO_UNDERSCORE_CHAR = "[^\\W_]";
    private static final String ALPHANUMERIC_NO_UNDERSCORE = ALPHANUMERIC_NO_UNDERSCORE_CHAR + "+";
    private static final String LOCAL_PART_REGEX = "^" + ALPHANUMERIC_NO_UNDERSCORE + "([" + SPECIAL_CHARACTERS + "]"
            + ALPHANUMERIC_NO_UNDERSCORE + ")*";
    private static final String DOMAIN_PART_REGEX = ALPHANUMERIC_NO_UNDERSCORE
            + "(-" + ALPHANUMERIC_NO_UNDERSCORE + ")*";
    // The top-level or final domain label must have at least two characters.
    // Specifying character count explicitly rather than repeating DOMAIN_PART_REGEX avoids nested quantifiers
    // that would otherwise cause catastrophic backtracking (ReDoS) on malformed inputs.
    private static final String DOMAIN_LAST_PART_REGEX = "(" + ALPHANUMERIC_NO_UNDERSCORE_CHAR + "{2,}|"
            + ALPHANUMERIC_NO_UNDERSCORE + "(-" + ALPHANUMERIC_NO_UNDERSCORE + ")+)$";
    private static final String DOMAIN_REGEX = "(" + DOMAIN_PART_REGEX + "\\.)*" + DOMAIN_LAST_PART_REGEX;
    public static final String VALIDATION_REGEX = LOCAL_PART_REGEX + "@" + DOMAIN_REGEX;
    private static final java.util.regex.Pattern VALIDATION_PATTERN = java.util.regex.Pattern.compile(VALIDATION_REGEX);

    public final String value;

    /**
     * Validates an email address and stores its trimmed, locale-independent lowercase form.
     *
     * @param email A valid email address.
     */
    public Email(String email) {
        requireNonNull(email);
        checkArgument(isValidEmail(email), MESSAGE_CONSTRAINTS);
        value = email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Returns true if a given string is a valid email.
     * Enforces RFC 5321 length guardrails (maximum 254 characters total, 64 characters for the local part,
     * and 255 characters for the domain part) before evaluating the validation pattern.
     */
    public static boolean isValidEmail(String test) {
        requireNonNull(test);
        String trimmed = test.trim();
        if (trimmed.length() > MAX_LENGTH) {
            return false;
        }
        int atIndex = trimmed.indexOf('@');
        if (atIndex <= 0 || atIndex > MAX_LOCAL_PART_LENGTH) {
            return false;
        }
        String domain = trimmed.substring(atIndex + 1);
        if (domain.isEmpty() || domain.length() > MAX_DOMAIN_PART_LENGTH) {
            return false;
        }
        return VALIDATION_PATTERN.matcher(trimmed).matches();
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
        if (!(other instanceof Email otherEmail)) {
            return false;
        }

        return value.equals(otherEmail.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
