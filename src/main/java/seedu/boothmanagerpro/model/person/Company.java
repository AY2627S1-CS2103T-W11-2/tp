package seedu.boothmanagerpro.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.commons.util.AppUtil.checkArgument;

import java.text.Normalizer;

/**
 * Immutable exhibitor organisation name, NFC-normalised, whitespace-collapsed, trimmed and limited to
 * 100 Unicode code points.
 *
 * <p>NFC normalisation and whitespace collapsing are applied at construction time so that canonically equivalent
 * representations (e.g. {@code é} as U+00E9 versus {@code e} followed by U+0301) and internal whitespace variants
 * (such as multiple spaces, tabs, or newlines) are stored identically. This ensures that case-insensitive duplicate
 * detection in {@link Person#isSamePerson(Person)} agrees with what the user sees on screen.
 *
 * <p>Value equality preserves case. Case-insensitive duplicate detection uses
 * {@link Person#isSamePerson(Person)}.
 */
public final class Company {
    public static final String MESSAGE_CONSTRAINTS = "Company name must contain 1 to 100 characters.";
    public final String value;

    /**
     * Creates a trimmed, NFC-normalised, whitespace-collapsed company name.
     *
     * @throws NullPointerException     If company is null.
     * @throws IllegalArgumentException If the normalised name is blank or exceeds 100 Unicode code points.
     */
    public Company(String company) {
        requireNonNull(company);
        String normalised = normaliseWhitespace(company);
        checkArgument(isValidCompany(normalised), MESSAGE_CONSTRAINTS);
        value = normalised;
    }

    /**
     * Returns whether a company name is non-blank and within the 100-code-point length limit.
     * The check is performed on the whitespace-normalised input; callers that have already normalised may pass
     * either form.
     */
    public static boolean isValidCompany(String company) {
        requireNonNull(company);
        String normalised = normaliseWhitespace(company);
        return !normalised.isBlank() && normalised.codePointCount(0, normalised.length()) <= 100;
    }

    /**
     * Returns a company name with Unicode NFC normalisation applied, surrounding whitespace removed,
     * and each run of internal whitespace (spaces, tabs, newlines) collapsed to a single space.
     *
     * <p>NFC (Canonical Decomposition followed by Canonical Composition) converts canonically
     * equivalent sequences — such as a base letter followed by a combining accent — into their
     * precomposed form. This makes byte-level string comparisons agree with what the user sees.
     */
    public static String normaliseWhitespace(String company) {
        requireNonNull(company);
        String nfc = Normalizer.normalize(company, Normalizer.Form.NFC);
        return nfc.strip().replaceAll("(?U)\\s+", " ");
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
