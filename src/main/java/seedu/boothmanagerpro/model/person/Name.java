package seedu.boothmanagerpro.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.commons.util.AppUtil.checkArgument;

/**
 * Represents a Person's name in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidName(String)}
 */
public class Name {

    public static final String MESSAGE_CONSTRAINTS =
            "Names must be 1 to 80 characters and contain a letter. "
            + "Use only letters, spaces, hyphens, apostrophes or full stops.";

    // Length and the presence of a letter are checked separately after whitespace normalisation.
    public static final String VALIDATION_REGEX = "[\\p{L}\\p{M} .'\u2019-]+";

    public final String fullName;

    /**
     * Trims, collapses consecutive spaces and validates a name of 1-80 Unicode code points with at least one letter.
     * Allowed characters are letters, spaces, hyphens, apostrophes and full stops.
     *
     * @param name A valid name.
     */
    public Name(String name) {
        requireNonNull(name);
        String normalisedName = normaliseWhitespace(name);
        checkArgument(isValidName(normalisedName), MESSAGE_CONSTRAINTS);
        fullName = normalisedName;
    }

    /**
     * Returns true if a given string is a valid name.
     */
    public static boolean isValidName(String test) {
        String normalised = normaliseWhitespace(test);
        return normalised.codePointCount(0, normalised.length()) <= 80 && normalised.matches(VALIDATION_REGEX)
                && normalised.codePoints().anyMatch(Character::isLetter);
    }

    /** Returns a name with surrounding whitespace removed and each run of spaces collapsed to one. */
    static String normaliseWhitespace(String name) {
        return name.trim().replaceAll(" +", " ");
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
