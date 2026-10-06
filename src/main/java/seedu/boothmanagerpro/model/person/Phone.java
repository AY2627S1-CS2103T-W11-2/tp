package seedu.boothmanagerpro.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's phone number in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidPhone(String)}
 */
public class Phone {


    public static final String MESSAGE_CONSTRAINTS =
            "Phone number must contain 7 to 15 digits.";
    public static final String VALIDATION_REGEX = "\\+?[0-9]{7,15}";
    public final String value;

    /**
     * Validates 7-15 digits with an optional leading plus and stores the value without spaces or hyphens.
     *
     * @param phone A valid phone number.
     */
    public Phone(String phone) {
        requireNonNull(phone);
        checkArgument(isValidPhone(phone), MESSAGE_CONSTRAINTS);
        value = normalize(phone);
    }

    /**
     * Returns true if a given string is a valid phone number.
     */
    public static boolean isValidPhone(String test) {
        return normalize(test).matches(VALIDATION_REGEX);
    }

    /** Removes accepted spaces and hyphens, preserving an optional leading plus. */
    private static String normalize(String phone) {
        return phone.trim().replace(" ", "").replace("-", "");
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
