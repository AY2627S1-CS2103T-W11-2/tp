package seedu.boothmanagerpro.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;

import seedu.boothmanagerpro.commons.util.ToStringBuilder;
import seedu.boothmanagerpro.model.person.Person;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    private final String feedbackToUser;

    /** The application should exit. */
    private final boolean exit;

    /** Contacts awaiting a numbered reply to a view request. */
    private final List<Person> viewChoices;

    /** Contacts the UI may present as cards alongside the text feedback. */
    private final List<Person> contactsToShow;

    /** Whether shown contacts carry the one-based indices used by edit and delete. */
    private final boolean numberedContacts;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean exit) {
        this(feedbackToUser, exit, List.of(), List.of(), false);
    }

    /**
     * Constructs text feedback with immutable snapshots of pending view choices and contacts to show.
     */
    private CommandResult(String feedbackToUser, boolean exit, List<Person> viewChoices,
            List<Person> contactsToShow, boolean numberedContacts) {
        this.viewChoices = List.copyOf(viewChoices);
        this.contactsToShow = List.copyOf(contactsToShow);
        this.numberedContacts = numberedContacts;
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.exit = exit;
    }

    /**
     * Constructs a {@code CommandResult} with the specified {@code feedbackToUser},
     * and other fields set to their default value.
     */
    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false);
    }

    /**
     * Creates a result prompting for a numbered choice among matching contacts.
     */
    public CommandResult(String feedbackToUser, List<Person> viewChoices) {
        this(feedbackToUser, false, viewChoices, List.of(), false);
    }

    /**
     * Creates text feedback whose contacts may also be shown as cards numbered from 1.
     */
    public static CommandResult showingContacts(String feedbackToUser, List<Person> contacts) {
        return new CommandResult(feedbackToUser, false, List.of(), contacts, true);
    }

    /**
     * Creates text feedback for one contact's details, which may also be shown as an unnumbered card.
     */
    public static CommandResult showingContact(String feedbackToUser, Person contact) {
        return new CommandResult(feedbackToUser, false, List.of(), List.of(requireNonNull(contact)), false);
    }

    public List<Person> getViewChoices() {
        return viewChoices;
    }

    public List<Person> getContactsToShow() {
        return contactsToShow;
    }

    public boolean isNumberedContacts() {
        return numberedContacts;
    }

    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    public boolean isExit() {
        return exit;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof CommandResult otherCommandResult)) {
            return false;
        }

        return feedbackToUser.equals(otherCommandResult.feedbackToUser)
                && exit == otherCommandResult.exit
                && viewChoices.equals(otherCommandResult.viewChoices)
                && contactsToShow.equals(otherCommandResult.contactsToShow)
                && numberedContacts == otherCommandResult.numberedContacts;
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, exit, viewChoices, contactsToShow, numberedContacts);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("exit", exit)
                .add("viewChoices", viewChoices)
                .add("contactsToShow", contactsToShow)
                .add("numberedContacts", numberedContacts)
                .toString();
    }

}
