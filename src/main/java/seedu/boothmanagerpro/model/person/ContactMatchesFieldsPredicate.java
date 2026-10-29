package seedu.boothmanagerpro.model.person;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Matches full names or name words and complete other fields, with OR within a field and AND across fields.
 * Criteria are copied so that changes to the caller's collections cannot affect a search.
 */
public class ContactMatchesFieldsPredicate implements Predicate<Person> {
    private final Map<String, Set<String>> criteria;

    /** Creates a predicate from normalised field values, defensively copying the supplied criteria. */
    public ContactMatchesFieldsPredicate(Map<String, Set<String>> criteria) {
        this.criteria = criteria.entrySet().stream().collect(Collectors.toUnmodifiableMap(
                Map.Entry::getKey, entry -> Set.copyOf(entry.getValue())));
    }

    /** Normalises search values, including spaces and hyphens in phone numbers. */
    public static String normalise(String field, String value) {
        if (field.equals("n/")) {
            return foldName(value);
        }
        if (field.equals("c/")) {
            return foldCompany(value);
        }
        String nfc = Normalizer.normalize(value, Normalizer.Form.NFC).strip();
        if (field.equals("t/")) {
            return foldCase(nfc);
        }
        String result = nfc.toLowerCase(Locale.ROOT);
        return field.equals("p/") ? result.replace(" ", "").replace("-", "") : result;
    }

    /**
     * Applies NFC Unicode normalisation, collapses whitespace (including spaces, tabs, and newlines),
     * then folds each code point to its case-equivalent.
     */
    private static String foldCompany(String value) {
        return foldCase(Company.normaliseWhitespace(value));
    }

    /**
     * Applies NFC Unicode normalisation, collapses whitespace, then folds each code point to its
     * case-equivalent using the same two-step (toUpperCase then toLowerCase) strategy as
     * {@link String#equalsIgnoreCase}.
     *
     * <p>NFC normalisation ensures that canonically equivalent representations — e.g. {@code é} as
     * U+00E9 versus {@code e} followed by combining accent U+0301 — produce the same folded string,
     * so {@code find n/José} finds contacts regardless of which Unicode encoding was used when they
     * were added.
     */
    private static String foldName(String value) {
        String nfc = Normalizer.normalize(value, Normalizer.Form.NFC);
        return foldCase(Name.normaliseWhitespace(nfc));
    }
    /** Folds code points using the same case equivalence as String.equalsIgnoreCase. */
    private static String foldCase(String value) {
        StringBuilder folded = new StringBuilder();
        value.codePoints().map(Character::toUpperCase).map(Character::toLowerCase)
                .forEach(folded::appendCodePoint);
        return folded.toString();
    }

    @Override
    public boolean test(Person person) {
        return criteria.entrySet().stream().allMatch(entry -> matches(person, entry.getKey(), entry.getValue()));
    }

    private boolean matches(Person person, String field, Set<String> values) {
        return switch (field) {
            case "n/" -> matchesName(person.getName().fullName, values);
            case "c/" -> person.getCompany()
                    .map(company -> values.contains(normalise(field, company.value))).orElse(false);
            case "e/" -> values.contains(normalise(field, person.getEmail().value));
            case "p/" -> values.contains(normalise(field, person.getPhone().value));
            case "t/" -> person.getTags().stream()
                    .anyMatch(tag -> values.contains(normalise(field, tag.tagName)));
            case "f/" -> person.getFollowUpDate()
                    .map(date -> values.contains(date.toIsoDate())
                            || values.contains(date.toString().toLowerCase(Locale.ROOT))).orElse(false);
            default -> false;
        };
    }

    private boolean matchesName(String fullName, Set<String> values) {
        String name = normalise("n/", fullName);
        return values.contains(name) || Arrays.stream(name.split(" "))
                .filter(word -> !Name.isPatronymic(word))
                .anyMatch(values::contains);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ContactMatchesFieldsPredicate predicate
                && criteria.equals(predicate.criteria);
    }

    @Override
    public int hashCode() {
        return criteria.hashCode();
    }

    @Override
    public String toString() {
        return criteria.toString();
    }
}
