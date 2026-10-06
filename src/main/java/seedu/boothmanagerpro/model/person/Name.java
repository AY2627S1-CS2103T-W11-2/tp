package seedu.boothmanagerpro.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
            "Names may contain letters, numbers, spaces, hyphens, apostrophes, and full stops, and must not be blank";

    // Preserve legacy numeric names while also accepting the punctuation used in exhibitor names.
    public static final String VALIDATION_REGEX = "(?=.*[\\p{L}\\p{N}])[\\p{L}\\p{M}\\p{N} .’'\\-]+";

    public final String fullName;

    /**
     * Trims and validates a name of 1-80 Unicode code points with at least one letter.
     * Allowed characters are letters, spaces, hyphens, apostrophes and full stops.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        checkArgument(isValidName(name), MESSAGE_CONSTRAINTS);
        fullName = name.trim();
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        String trimmed = test.trim();
        return trimmed.codePointCount(0, trimmed.length()) <= 80 && trimmed.matches(VALIDATION_REGEX)
                && trimmed.codePoints().anyMatch(Character::isLetter);
    }


    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Name otherName)) {
            return false;
        }

        return fullName.equals(otherName.fullName);
    }

    @Override
    public int hashCode() {
        return fullName.hashCode();
    }

}
