package seedu.boothmanagerpro.model.person;

import static java.util.Objects.requireNonNull;

/** The manually assigned stage of an exhibitor enquiry; new contacts need follow-up by default. */
public enum EnquiryStatus {
    NEEDS_FOLLOW_UP("Needs follow-up"),
    IN_PROGRESS("In progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    public static final String MESSAGE_CONSTRAINTS =
            "Enquiry status must be Needs follow-up, In progress, Completed, or Cancelled.";

    private final String label;

    EnquiryStatus(String label) {
        this.label = label;
    }

    /** Parses a trimmed, case-insensitive display label without accepting unknown or empty stages. */
    public static EnquiryStatus fromString(String value) {
        requireNonNull(value);
        for (EnquiryStatus status : values()) {
            if (status.label.equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
    }

    @Override
    public String toString() {
        return label;
    }
}
