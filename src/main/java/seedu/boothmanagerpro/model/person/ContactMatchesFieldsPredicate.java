package seedu.boothmanagerpro.model.person;

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

    /** Normalises values for exact matching, including spaces and hyphens in phone numbers. */
    public static String normalise(String field, String value) {
        if (field.equals("n/")) {
            return foldName(value);
        }
        String result = value.trim().toLowerCase(Locale.ROOT);
        return field.equals("p/") ? result.replace(" ", "").replace("-", "") : result;
    }

    /** Normalises spaces and folds each name code point using the same case equivalence as String.equalsIgnoreCase. */
    private static String foldName(String value) {
        StringBuilder folded = new StringBuilder();
        Name.normaliseWhitespace(value).codePoints().map(Character::toUpperCase).map(Character::toLowerCase)
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
                    .map(date -> values.contains(date.toString())).orElse(false);
            default -> false;
        };
    }

    private boolean matchesName(String fullName, Set<String> values) {
        String name = normalise("n/", fullName);
        return values.contains(name) || Arrays.stream(name.split(" ")).anyMatch(values::contains);
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
