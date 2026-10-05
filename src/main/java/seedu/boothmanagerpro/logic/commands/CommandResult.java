package seedu.boothmanagerpro.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.boothmanagerpro.commons.util.ToStringBuilder;
import seedu.boothmanagerpro.model.person.Person;

/**
 * Represents the result of a command execution.
 */
public class CommandResult {

    private final String feedbackToUser;

    /** Help information should be shown to the user. */
    private final boolean showHelp;

    /** The application should exit. */
    private final boolean exit;

    /** Contacts awaiting a numbered reply to a view request. */
    private final List<Person> viewChoices;

    /** Contact whose details should be shown, when a view selection has been resolved. */
    private final Person personToView;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean showHelp, boolean exit) {
        this(feedbackToUser, showHelp, exit, List.of(), null);
    }

    /**
     * Constructs a result with an immutable snapshot of the choices and an optional selected contact.
     */
    private CommandResult(String feedbackToUser, boolean showHelp, boolean exit,
            List<Person> viewChoices, Person personToView) {
        this.personToView = personToView;
        this.viewChoices = List.copyOf(viewChoices);
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.showHelp = showHelp;
        this.exit = exit;
    }

    /**
     * Constructs a {@code CommandResult} with the specified {@code feedbackToUser},
     * and other fields set to their default value.
     */
    public CommandResult(String feedbackToUser) {
        this(feedbackToUser, false, false);
    }

    /**
     * Creates a result prompting for a numbered choice among matching contacts.
     */
    public CommandResult(String feedbackToUser, List<Person> viewChoices) {
        this(feedbackToUser, false, false, viewChoices, null);
    }

    /**
     * Creates a result requesting that the selected contact be displayed in the GUI.
     */
    public CommandResult(String feedbackToUser, Person personToView) {
        this(feedbackToUser, false, false, List.of(), requireNonNull(personToView));
    }

    public Optional<Person> getPersonToView() {
        return Optional.ofNullable(personToView);
    }

    public List<Person> getViewChoices() {
        return viewChoices;
    }

    public String getFeedbackToUser() {
        return feedbackToUser;
    }

    public boolean isShowHelp() {
        return showHelp;
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
                && showHelp == otherCommandResult.showHelp
                && exit == otherCommandResult.exit
                && viewChoices.equals(otherCommandResult.viewChoices)
                && Objects.equals(personToView, otherCommandResult.personToView);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, showHelp, exit, viewChoices, personToView);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("showHelp", showHelp)
                .add("exit", exit)
                .add("viewChoices", viewChoices)
                .add("personToView", personToView)
                .toString();
    }

}
