package seedu.boothmanagerpro.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's phone number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {

    public static final String MESSAGE_CONSTRAINTS =
            "Phone number must contain 7 to 15 digits, optionally starting with +. Single spaces are allowed "
                    + "between digit groups. Example: +65 9123 4567.";
    public static final int MAX_INPUT_LENGTH = 30;
    private static final String PHONE_FORMAT_REGEX = "\\+?[0-9]+(?: [0-9]+)*";
    private static final String ALLOWED_CHARACTERS_REGEX = "\\+?(?:[0-9]+(?: [0-9]+)*)?";

    public final String value;

    /**
     * Validates and stores a phone number after removing surrounding whitespace.
     *
     * @param phone A valid phone number.
     */
    public Phone(String phone) {
        requireNonNull(phone);
        checkArgument(isValidPhone(phone), MESSAGE_CONSTRAINTS);
        value = phone.strip();
    }

    /**
     * Returns true if the value contains 7 to 15 digits with an optional leading {@code +} and single spaces
     * between digit groups.
     */
    public static boolean isValidPhone(String test) {
        requireNonNull(test);
        String trimmed = test.strip();
        long digitCount = trimmed.chars().filter(Character::isDigit).count();
        return trimmed.length() <= MAX_INPUT_LENGTH && trimmed.matches(PHONE_FORMAT_REGEX)
                && digitCount >= 7 && digitCount <= 15;
    }

    /**
     * Returns true if {@code test} contains only digits, correctly placed single spaces and, optionally, one
     * leading {@code +}.
     */
    public static boolean hasAllowedCharacters(String test) {
        requireNonNull(test);
        return test.strip().matches(ALLOWED_CHARACTERS_REGEX);
    }

    /** Returns the trimmed comparison form without permitted spaces. */
    public static String normalize(String rawPhone) {
        return rawPhone.strip().replace(" ", "");
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
