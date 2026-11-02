package seedu.boothmanagerpro.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.commons.util.AppUtil.checkArgument;

/** An inclusive calendar interval for a follow-up search. */
public record FollowUpDateRange(FollowUpDate start, FollowUpDate end) {
    public static final String MESSAGE_CONSTRAINTS =
            "Date range must use two valid YYYYMMDD dates separated by '..', with start on or before end.";

    /** Validates both endpoints and rejects reversed intervals. */
    public FollowUpDateRange {
        requireNonNull(start);
        requireNonNull(end);
        checkArgument(!start.value.isAfter(end.value), MESSAGE_CONSTRAINTS);
    }

    /** Parses two strict calendar dates separated by two full stops. */
    public static FollowUpDateRange parse(String input) {
        requireNonNull(input);
        String[] endpoints = input.strip().split("\\.\\.", -1);
        checkArgument(endpoints.length == 2, MESSAGE_CONSTRAINTS);
        try {
            FollowUpDate first = new FollowUpDate(endpoints[0]);
            FollowUpDate last = new FollowUpDate(endpoints[1]);
            return new FollowUpDateRange(first, last);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS, e);
        }
    }

    /** Includes contacts on either endpoint as well as dates between them. */
    public boolean matches(FollowUpDate date) {
        requireNonNull(date);
        return !date.value.isBefore(start.value) && !date.value.isAfter(end.value);
    }
}
