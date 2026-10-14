package seedu.boothmanagerpro.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.boothmanagerpro.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;

import seedu.boothmanagerpro.logic.Messages;
import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.model.person.Person;

/**
 * Lists all exhibitor contacts in the address book to the user.
 */
public class ListCommand extends Command {

    public static final String COMMAND_WORD = "list";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Lists all exhibitor contacts. "
            + "This command does not accept parameters.\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_HEADER = "Exhibitor Contact List";
    public static final String MESSAGE_DIVIDER = "─".repeat(38);
    public static final String MESSAGE_TOTAL = "Total contacts: %d";
    public static final String MESSAGE_EMPTY = "No contacts found.\n"
            + "Use the 'add' command to add a new exhibitor contact.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        List<Person> contacts = model.getFilteredPersonList();
        return CommandResult.showingContacts(formatContactList(contacts), contacts);
    }

    /**
     * Returns the contact list feedback for {@code persons}, or the empty-list guidance when there are none.
     */
    public static String formatContactList(List<Person> persons) {
        requireNonNull(persons);
        if (persons.isEmpty()) {
            return MESSAGE_EMPTY;
        }
        String contacts = Messages.formatNumberedContacts(persons);
        return MESSAGE_HEADER + "\n" + MESSAGE_DIVIDER + "\n\n"
                + contacts + "\n\n"
                + MESSAGE_DIVIDER + "\n" + String.format(MESSAGE_TOTAL, persons.size());
    }

}
