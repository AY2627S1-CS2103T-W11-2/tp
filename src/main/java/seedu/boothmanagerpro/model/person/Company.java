package seedu.boothmanagerpro.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.commons.util.AppUtil.checkArgument;

import java.text.Normalizer;

/**
 * Immutable exhibitor organisation name, NFC-normalised, trimmed and limited to 100 Unicode code points.
 *
 * <p>NFC normalisation is applied at construction time so that canonically equivalent representations
 * (e.g. {@code é} as U+00E9 versus {@code e} followed by U+0301) are stored identically. This ensures
 * that case-insensitive duplicate detection in {@link Person#isSamePerson(Person)} agrees with what
 * the user sees on screen.
 *
 * <p>Value equality preserves case. Case-insensitive duplicate detection uses
 * {@link Person#isSamePerson(Person)}.
 */
public final class Company {
    public static final String MESSAGE_CONSTRAINTS = "Company name must contain 1 to 100 characters.";
    public final String value;

    /**
     * Creates a trimmed, NFC-normalised company name.
     *
     * @throws NullPointerException     If company is null.
     * @throws IllegalArgumentException If the trimmed name is blank or exceeds 100 Unicode code points.
     */
    public Company(String company) {
        requireNonNull(company);
        String nfc = Normalizer.normalize(company.trim(), Normalizer.Form.NFC);
        checkArgument(isValidCompany(nfc), MESSAGE_CONSTRAINTS);
        value = nfc;
    }

    /**
     * Returns whether a company name is non-blank and within the 100-code-point length limit.
     * The check is performed on the trimmed input; callers that have already trimmed may pass
     * either form.
     */
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
