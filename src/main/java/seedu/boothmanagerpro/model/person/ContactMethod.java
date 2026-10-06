package seedu.boothmanagerpro.model.person;

import java.util.Locale;

/** Supported ways to contact an exhibitor representative. Absence is represented by Optional.empty(). */
public enum ContactMethod {
    EMAIL, PHONE, OTHER;

    public static final String MESSAGE_CONSTRAINTS = "Contact method must be email, phone, or other.";

    /**
     * Parses a trimmed, case-insensitive method using locale-independent casing.
     * Omission is handled by the caller; an empty string is invalid, not {@code OTHER}.
     *
     * @throws NullPointerException If value is null.
     * @throws IllegalArgumentException If value is not email, phone or other.
     */
    public static ContactMethod fromString(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS, e);
        }
    }

    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
