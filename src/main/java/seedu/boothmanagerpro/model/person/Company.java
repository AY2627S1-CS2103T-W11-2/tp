package seedu.boothmanagerpro.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.commons.util.AppUtil.checkArgument;

/**
 * Immutable exhibitor organisation name, trimmed and limited to 100 Unicode code points.
 * Value equality preserves case. Case-insensitive duplicate detection uses {@link Person#isSamePerson(Person)}.
 */
public final class Company {
    public static final String MESSAGE_CONSTRAINTS = "Company name must contain 1 to 100 characters.";
    public final String value;

    /**
     * Creates a trimmed company name.
     *
     * @throws NullPointerException If company is null.
     * @throws IllegalArgumentException If the trimmed name is blank or exceeds 100 Unicode code points.
     */
    public Company(String company) {
        requireNonNull(company);
        checkArgument(isValidCompany(company), MESSAGE_CONSTRAINTS);
        value = company.trim();
    }

    /** Returns whether a company name is non-blank and within the length limit. */
    public static boolean isValidCompany(String company) {
        String trimmed = company.trim();
        return !trimmed.isBlank() && trimmed.codePointCount(0, trimmed.length()) <= 100;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof Company company && value.equals(company.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
