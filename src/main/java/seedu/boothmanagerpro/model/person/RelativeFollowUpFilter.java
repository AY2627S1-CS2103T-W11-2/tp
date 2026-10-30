package seedu.boothmanagerpro.model.person;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;

/** Date-only follow-up filters, evaluated against the calendar date captured for a search. */
public enum RelativeFollowUpFilter {
    TODAY,
    DUE,
    OVERDUE;

    /** Recognises a relative date keyword without accepting invalid literal calendar dates. */
    public static Optional<RelativeFollowUpFilter> fromKeyword(String input) {
        requireNonNull(input);
        String keyword = input.strip().toUpperCase(Locale.ROOT);
        for (RelativeFollowUpFilter filter : values()) {
            if (filter.name().equals(keyword)) {
                return Optional.of(filter);
            }
        }
        return Optional.empty();
    }

    /** DUE includes today; OVERDUE includes only earlier dates. Neither implies an enquiry status. */
    public boolean matches(FollowUpDate date, LocalDate referenceDate) {
        requireNonNull(date);
        requireNonNull(referenceDate);
        return switch (this) {
            case TODAY -> date.value.equals(referenceDate);
            case DUE -> !date.value.isAfter(referenceDate);
            case OVERDUE -> date.value.isBefore(referenceDate);
        };
    }
}
