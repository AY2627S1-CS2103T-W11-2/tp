package seedu.boothmanagerpro.logic.parser;

import java.util.function.Function;

import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;

/** Shared field conversion for add and update; validation remains owned by model value types. */
final class ContactFieldParser {
    private ContactFieldParser() {}

    /** Rejects a blank field using the common add and update error. */
    static void requireValue(Prefix prefix, String value) throws ParseException {
        if (value.isBlank()) {
            throw new ParseException("Empty field: " + prefix + ". Supply a value after this prefix.");
        }
    }

    /** Adds prefix-specific feedback without duplicating model constraints or normalisation. */
    static <T> T parseValue(Prefix prefix, String value, Function<String, T> constructor) throws ParseException {
        requireValue(prefix, value);
        try {
            return constructor.apply(value);
        } catch (IllegalArgumentException e) {
            throw new ParseException("Invalid value for " + prefix + ": " + e.getMessage(), e);
        }
    }
}
