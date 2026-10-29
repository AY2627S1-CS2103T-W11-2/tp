package seedu.boothmanagerpro.model.tag;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.commons.util.AppUtil.checkArgument;

import java.text.Normalizer;

import seedu.boothmanagerpro.commons.util.StringUtil;

/**
 * Represents a Tag in the address book.
 * Guarantees: immutable; name is valid as declared in {@link #isValidTagName(String)}
 */
public class Tag {

    public static final int MAX_LENGTH = 30;
    public static final String MESSAGE_CONSTRAINTS = "Tag must be 1 to 30 characters and cannot contain '/'.";
    public static final String VALIDATION_REGEX = "[^/\\r\\n]{1,30}";

    public final String tagName;

    /**
     * Creates a trimmed, NFC-normalised, non-blank tag of at most 30 characters without the reserved slash character,
     * collapsing consecutive internal spaces.
     * Equality is case-insensitive while preserving display casing, so case variants collapse in a set.
     *
     * @param tagName A valid tag name.
     */
    public Tag(String tagName) {
        requireNonNull(tagName);
        String normalised = normaliseWhitespace(tagName);
        checkArgument(isValidTagName(normalised), MESSAGE_CONSTRAINTS);
        this.tagName = normalised;
    }

    /**
     * Returns true if a given string is a valid tag name.
     * Enforces that the tag is non-blank, contains no slash or newline characters, and does not exceed
     * {@link #MAX_LENGTH} Unicode code points. Supports tags in any language.
     */
    public static boolean isValidTagName(String test) {
        requireNonNull(test);
        String trimmed = normaliseWhitespace(test);
        return !trimmed.isBlank()
                && !trimmed.contains("/")
                && !trimmed.contains("\r")
                && !trimmed.contains("\n")
                && trimmed.codePointCount(0, trimmed.length()) <= MAX_LENGTH;
    }

    /**
     * Returns a tag name with Unicode NFC normalisation applied, surrounding whitespace removed,
     * and runs of non-linebreak whitespace collapsed to a single space.
     */
    public static String normaliseWhitespace(String tagName) {
        requireNonNull(tagName);
        return Normalizer.normalize(tagName, Normalizer.Form.NFC)
                .strip()
                .replaceAll("(?U)[\\s&&[^\\r\\n]]+", " ");
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Tag otherTag)) {
            return false;
        }

        return StringUtil.foldCase(tagName).equals(StringUtil.foldCase(otherTag.tagName));
    }

    @Override
    public int hashCode() {
        return StringUtil.foldCase(tagName).hashCode();
    }

    /**
     * Formats state as text for viewing.
     */
    public String toString() {
        return '[' + tagName + ']';
    }

}
