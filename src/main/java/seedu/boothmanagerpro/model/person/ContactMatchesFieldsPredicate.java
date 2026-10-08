package seedu.boothmanagerpro.model.person;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Matches complete contact fields: alternatives within a field use OR, different fields use AND.
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
        String result = value.trim().toLowerCase(Locale.ROOT);
        return field.equals("p/") ? result.replace(" ", "").replace("-", "") : result;
    }

    @Override
    public boolean test(Person person) {
        return criteria.entrySet().stream().allMatch(entry -> matches(person, entry.getKey(), entry.getValue()));
    }

    private boolean matches(Person person, String field, Set<String> values) {
        return switch (field) {
            case "n/" -> values.contains(normalise(field, person.getName().fullName));
            case "c/" -> person.getCompany()
                    .map(company -> values.contains(normalise(field, company.value))).orElse(false);
            case "e/" -> values.contains(normalise(field, person.getEmail().value));
            case "p/" -> values.contains(normalise(field, person.getPhone().value));
            case "t/" -> person.getTags().stream()
                    .anyMatch(tag -> values.contains(normalise(field, tag.tagName)));
            default -> false;
        };
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
