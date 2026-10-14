package seedu.boothmanagerpro.logic;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import seedu.boothmanagerpro.logic.parser.Prefix;
import seedu.boothmanagerpro.model.person.Person;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX = "The contact index provided is invalid.";
    public static final String MESSAGE_PERSONS_LISTED_OVERVIEW = "%1$d contact(s) found.";
    public static final String EMPTY_TAGS = "None";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toCollection(LinkedHashSet::new));

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Formats the {@code person} for display to the user.
     */
    public static String format(Person person) {
        final StringBuilder builder = new StringBuilder();
        builder.append(person.getName())
                .append("; Company: ")
                .append(formatOptional(person.getCompany()))
                .append("; Contact method: ")
                .append(formatOptional(person.getContactMethod()))
                .append("; Phone: ")
                .append(person.getPhone())
                .append("; Email: ")
                .append(person.getEmail())
                .append("; Address: ")
                .append(person.getAddress())
                .append("; Tags: ")
                .append(formatTags(person));
        return builder.toString();
    }

    /** Formats the same contact details for list and view, retaining stored tag order. */
    public static String formatContactDetails(Person person) {
        return person.getName() + " at " + formatOptional(person.getCompany())
                + "\nEmail: " + person.getEmail()
                + "\nPhone: " + person.getPhone()
                + "\nContact method: " + formatOptional(person.getContactMethod())
                + "\nTags: " + formatTags(person);
    }

    /** Numbers text results using the same one-based indices as edit and delete. */
    public static String formatNumberedContacts(List<Person> persons) {
        return IntStream.range(0, persons.size())
                .mapToObj(index -> (index + 1) + ". " + formatContactDetails(persons.get(index)))
                .collect(Collectors.joining("\n\n"));
    }

    /** Formats tags in stored order with a consistent empty-state label. */
    public static String formatTags(Person person) {
        return person.getTags().isEmpty() ? EMPTY_TAGS
                : person.getTags().stream().map(tag -> tag.tagName).collect(Collectors.joining(", "));
    }

    /** Formats an optional contact field for command feedback and the UI. */
    public static String formatOptional(Optional<?> value) {
        return value.map(Object::toString).orElse("Not specified");
    }

}
