package seedu.boothmanagerpro.model.person;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;

/**
 * A field the displayed contact list can be sorted by. Text uses locale-aware collation ignoring case, and every
 * order ends with the unique email so contacts never tie and their displayed indices stay stable.
 */
public enum SortField {
    NAME(Orders.BY_NAME.thenComparing(Orders.BY_COMPANY_MISSING_LAST).thenComparing(Orders.BY_EMAIL)),
    COMPANY(Orders.BY_COMPANY_MISSING_LAST.thenComparing(Orders.BY_NAME).thenComparing(Orders.BY_EMAIL)),
    /** Follows the enquiry workflow, so contacts needing follow-up come first and completed ones last. */
    STATUS(Orders.BY_STATUS.thenComparing(Orders.BY_NAME).thenComparing(Orders.BY_EMAIL));

    public static final String MESSAGE_CONSTRAINTS = "Contacts can only be sorted by name, company, or status.";

    private final Comparator<Person> comparator;

    SortField(Comparator<Person> comparator) {
        this.comparator = comparator;
    }

    public Comparator<Person> getComparator() {
        return comparator;
    }

    /** Returns the field named by {@code keyword}, ignoring case and surrounding whitespace. */
    public static Optional<SortField> fromKeyword(String keyword) {
        String normalised = keyword.strip().toUpperCase(Locale.ROOT);
        for (SortField field : values()) {
            if (field.name().equals(normalised)) {
                return Optional.of(field);
            }
        }
        return Optional.empty();
    }

    /** Returns the keyword users type to choose this field. */
    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** Holds the building-block orders, which enum constants cannot reference as static fields of the enum. */
    private static final class Orders {
        /**
         * Locale-aware collation that ignores case but not accents, so {@code Élodie} sorts beside {@code Elodie}
         * (rather than after {@code Zoe}) and {@code alicia tan} ties with {@code Alicia Tan}.
         */
        private static final Collator TEXT_ORDER = createTextOrder();
        private static final Comparator<Person> BY_NAME =
                Comparator.comparing(person -> person.getName().fullName, TEXT_ORDER);
        /** Contacts without a company, which only older records can have, are placed after those with one. */
        private static final Comparator<Person> BY_COMPANY_MISSING_LAST =
                Comparator.<Person, Boolean>comparing(person -> person.getCompany().isEmpty())
                        .thenComparing(person -> person.getCompany().map(company -> company.value).orElse(""),
                                TEXT_ORDER);
        private static final Comparator<Person> BY_EMAIL = Comparator.comparing(person -> person.getEmail().value);
        /** Uses the declaration order of {@link EnquiryStatus}: needs follow-up, in progress, completed. */
        private static final Comparator<Person> BY_STATUS = Comparator.comparing(Person::getEnquiryStatus);

        private static Collator createTextOrder() {
            Collator collator = Collator.getInstance(Locale.ROOT);
            collator.setStrength(Collator.SECONDARY);
            collator.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
            return collator;
        }
    }
}
