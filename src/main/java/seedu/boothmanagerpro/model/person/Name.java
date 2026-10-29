package seedu.boothmanagerpro.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.commons.util.AppUtil.checkArgument;

import java.text.Normalizer;

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
     * Trims, NFC-normalises, collapses consecutive spaces and validates a name of 1-80 Unicode
     * code points with at least one letter.
     * Allowed characters are letters (including Unicode), combining marks, spaces, hyphens,
     * apostrophes and full stops.
     *
     * <p>NFC normalisation ensures that canonically equivalent representations (e.g. {@code é} as
     * U+00E9 versus {@code e} followed by U+0301) are stored identically, so duplicate detection
     * and case-insensitive comparisons are consistent with what the user sees on screen.
     *
     * @param name A valid name string.
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

    /**
     * Returns a name with Unicode NFC normalisation applied, surrounding whitespace removed,
     * and each run of spaces collapsed to one.
     *
     * <p>NFC (Canonical Decomposition followed by Canonical Composition) converts canonically
     * equivalent sequences — such as a base letter followed by a combining accent — into their
     * precomposed form. This makes byte-level string comparisons agree with what the user sees.
     */
    static String normaliseWhitespace(String name) {
        String nfc = Normalizer.normalize(name, Normalizer.Form.NFC);
        return nfc.strip().replaceAll(" +", " ");
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
