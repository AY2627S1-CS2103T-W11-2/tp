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

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean exit) {
        this(feedbackToUser, exit, List.of());
    }

    /**
     * Constructs text feedback with an immutable snapshot of pending numbered view choices.
     */
    private CommandResult(String feedbackToUser, boolean exit,
            List<Person> viewChoices) {
        this.viewChoices = List.copyOf(viewChoices);
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
        this(feedbackToUser, false, viewChoices);
    }

    public List<Person> getViewChoices() {
        return viewChoices;
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
                && viewChoices.equals(otherCommandResult.viewChoices);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, exit, viewChoices);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("exit", exit)
                .add("viewChoices", viewChoices)
                .toString();
    }

}
