package seedu.boothmanagerpro.model.person;

import static java.util.Objects.requireNonNull;

/**
 * Represents a free-text remark about a contact. Any value is accepted, including an empty string,
 * which means the contact has no remark.
 * Guarantees: immutable.
 */
public class Remark {

    public final String value;

    /**
     * Constructs a {@code Remark}.
     *
     * @param remark Any text, or an empty string for no remark.
     */
    public Remark(String remark) {
        requireNonNull(remark);
        value = remark;
    }

    /** Returns true if this remark has no text. */
    public boolean isEmpty() {
        return value.isEmpty();
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
        if (!(other instanceof Remark otherRemark)) {
            return false;
        }

        return value.equals(otherRemark.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
