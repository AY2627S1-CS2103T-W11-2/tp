package seedu.boothmanagerpro.model.person;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Immutable calendar date with strict YYYYMMDD input, no time zone, and years 0001 through 9999. */
public final class FollowUpDate {
    public static final String MESSAGE_CONSTRAINTS =
            "Follow-up date must be a valid date in YYYYMMDD format (years 0001 to 9999), e.g. 20261025.";

    public final LocalDate value;

    /** Validates actual calendar dates, including leap years; past dates are allowed. */
    public FollowUpDate(String input) {
        requireNonNull(input);
        String trimmed = input.trim();
        if (!trimmed.matches("[0-9]{8}")) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        try {
            value = LocalDate.parse(trimmed, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS, e);
        }
        if (value.getYear() == 0) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
    }

    @Override
    public String toString() {
        return value.format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof FollowUpDate date && value.equals(date.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
