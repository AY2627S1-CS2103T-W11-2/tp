package seedu.boothmanagerpro.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;
import java.util.Optional;

import seedu.boothmanagerpro.commons.util.ToStringBuilder;
import seedu.boothmanagerpro.logic.Messages;
import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.model.person.Person;
import seedu.boothmanagerpro.model.person.SortField;

/**
 * Lists all exhibitor contacts in the address book to the user, in stored order or sorted by a field.
 * Sorting changes only the displayed order, so indices used by later commands follow the sorted list.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Lists all exhibitor contacts in the order they "
            + "were added, or sorted by name, company, or enquiry status.\n"
            + "Parameters: [o/name], [o/company], or [o/status]\n"
            + "Examples: " + COMMAND_WORD + ", " + COMMAND_WORD + " o/name, " + COMMAND_WORD + " o/status";

    public static final String MESSAGE_HEADER = "Exhibitor Contact List";
    public static final String MESSAGE_SORTED_HEADER = MESSAGE_HEADER + " (sorted by %s)";
    public static final int DIVIDER_LENGTH = 38;
    public static final String MESSAGE_DIVIDER = "─".repeat(DIVIDER_LENGTH);
    public static final String MESSAGE_TOTAL = "Total contacts: %d";
    public static final String MESSAGE_EMPTY = "No contacts found.\n"
            + "Use the 'add' command to add a new exhibitor contact.";

    private final Optional<SortField> sortField;

    /** Creates a command that lists contacts in stored order, clearing any previous sort. */
    public ListCommand() {
        this.sortField = Optional.empty();
    }

    /** Creates a command that lists contacts sorted by {@code sortField}. */
    public ListCommand(SortField sortField) {
        this.sortField = Optional.of(requireNonNull(sortField));
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        sortField.ifPresentOrElse(field -> model.updateSortedPersonList(field.getComparator()),
                model::resetPersonListOrder);
        List<Person> contacts = model.getFilteredPersonList();
        String header = sortField.map(field -> String.format(MESSAGE_SORTED_HEADER, field)).orElse(MESSAGE_HEADER);
        return CommandResult.showingContactList(formatContactList(contacts, header), contacts);
    }

    /**
     * Returns the contact list feedback for {@code persons} under {@code header}, or the empty-list guidance.
     */
    public static String formatContactList(List<Person> persons, String header) {
        requireNonNull(persons);
        requireNonNull(header);
        if (persons.isEmpty()) {
            return MESSAGE_EMPTY;
        }
        String contacts = Messages.formatNumberedContacts(persons);
        return header + "\n" + MESSAGE_DIVIDER + "\n\n"
                + contacts + "\n\n"
                + MESSAGE_DIVIDER + "\n" + String.format(MESSAGE_TOTAL, persons.size());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ListCommand otherCommand)) {
            return false;
        }

        return sortField.equals(otherCommand.sortField);
    }

    @Override
    public int hashCode() {
        return sortField.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("sortField", sortField).toString();
    }

}
