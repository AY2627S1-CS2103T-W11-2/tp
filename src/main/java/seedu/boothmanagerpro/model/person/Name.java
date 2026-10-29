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
            + "Use only letters, digits, spaces, hyphens, apostrophes, full stops, commas, parentheses, @, "
            + "or s/o, d/o, a/l and a/p between words.";

    private static final String WORD_REGEX = "[\\p{L}\\p{M}\\p{N}.,'\u2019@()-]+";
    private static final String PATRONYMIC_REGEX = "(?i:[sd]/o|a/[lp])";
    private static final String PATRONYMIC_SEPARATOR_REGEX = " " + PATRONYMIC_REGEX + " ";

    // Length and the presence of a letter in each segment are checked separately after whitespace normalisation.
    public static final String VALIDATION_REGEX =
            WORD_REGEX + "(?: (?:(?:" + PATRONYMIC_REGEX + ") )?" + WORD_REGEX + ")*";

    public final String fullName;

    /**
     * Trims, NFC-normalises, collapses consecutive spaces and validates a name of 1-80 Unicode
     * code points with at least one letter.
     * Allowed characters are letters (including Unicode), combining marks, digits, spaces, hyphens,
     * apostrophes, full stops, commas, parentheses, {@code @}, and patronymic markers ({@code s/o}, {@code d/o},
     * {@code a/l} and {@code a/p}) between words.
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
        if (normalised.codePointCount(0, normalised.length()) > 80 || !normalised.matches(VALIDATION_REGEX)) {
            return false;
        }
        for (String segment : normalised.split(PATRONYMIC_SEPARATOR_REGEX, -1)) {
            if (segment.codePoints().noneMatch(Character::isLetter)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the given word is a patronymic marker ({@code s/o}, {@code d/o}, {@code a/l} or
     * {@code a/p}, case-insensitive).
     */
    public static boolean isPatronymic(String word) {
        if (word == null) {
            return false;
        }
        return word.strip().matches(PATRONYMIC_REGEX);
    }

    /**
     * Returns a name with Unicode NFC normalisation applied, surrounding whitespace removed,
     * curly apostrophes ({@code \u2019}) normalised to straight apostrophes ({@code '}),
     * and each run of whitespace (including pasted tabs and line breaks) collapsed to one space.
     *
     * <p>NFC (Canonical Decomposition followed by Canonical Composition) converts canonically
     * equivalent sequences — such as a base letter followed by a combining accent — into their
     * precomposed form. This makes byte-level string comparisons agree with what the user sees.
     */
    static String normaliseWhitespace(String name) {
        String nfc = Normalizer.normalize(name, Normalizer.Form.NFC);
        return nfc.replace('\u2019', '\'').replaceAll("(?U)\\s+", " ").strip();
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
