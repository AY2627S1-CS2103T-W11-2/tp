package seedu.boothmanagerpro.logic.parser;

import static seedu.boothmanagerpro.logic.parser.CliSyntax.PREFIX_PHONE;

import java.util.function.Function;

import seedu.boothmanagerpro.logic.parser.exceptions.ParseException;
import seedu.boothmanagerpro.model.person.Phone;

/** Shared field conversion for add and update; validation remains owned by model value types. */
final class ContactFieldParser {
    static final String MESSAGE_EMPTY_FIELD = "Empty field: %s. Supply a value after this prefix.";
    static final String MESSAGE_INVALID_VALUE = "Invalid value for %s: %s";

    private ContactFieldParser() {}

    /** Rejects a blank field using the common add, edit and update error. */
    static void requireValue(Prefix prefix, String value) throws ParseException {
        if (value.isBlank()) {
            throw new ParseException(String.format(MESSAGE_EMPTY_FIELD, prefix));
        }
    }

    /** Adds prefix-specific feedback without duplicating model constraints or normalisation. */
    static <T> T parseValue(Prefix prefix, String value, Function<String, T> constructor) throws ParseException {
        requireValue(prefix, value);
        try {
            return constructor.apply(value);
        } catch (IllegalArgumentException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_VALUE, prefix, e.getMessage()), e);
        }
    }

    /** Parses a phone, explaining whether a disallowed character or the digit count made it invalid. */
    static Phone parsePhone(String value) throws ParseException {
        requireValue(PREFIX_PHONE, value);
        try {
            return ParserUtil.parsePhone(value);
        } catch (ParseException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_VALUE, PREFIX_PHONE, e.getMessage()), e);
        }
    }
}
