package seedu.boothmanagerpro.model.tag;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.commons.util.AppUtil.checkArgument;

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
     * Creates a trimmed, non-blank tag of at most 30 characters without the reserved slash character.
     * Equality is case-sensitive, so only identical trimmed tags collapse in a set.
     *
     * @param tagName A valid tag name.
     */
    public Tag(String tagName) {
        requireNonNull(tagName);
        checkArgument(isValidTagName(tagName), MESSAGE_CONSTRAINTS);
        this.tagName = tagName.trim();
    }

    /**
     * Returns true if a given string is a valid tag name.
     * Enforces that the tag is non-blank, contains no slash or newline characters, and does not exceed
     * {@link #MAX_LENGTH} Unicode code points. Supports tags in any language.
     */
    public static boolean isValidTagName(String test) {
        requireNonNull(test);
        String trimmed = test.trim();
        return !trimmed.isBlank()
                && !trimmed.contains("/")
                && !trimmed.contains("\r")
                && !trimmed.contains("\n")
                && trimmed.codePointCount(0, trimmed.length()) <= MAX_LENGTH;
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

        return tagName.equals(otherTag.tagName);
    }

    @Override
    public int hashCode() {
        return tagName.hashCode();
    }

    /**
     * Formats state as text for viewing.
     */
    public String toString() {
        return '[' + tagName + ']';
    }

}
