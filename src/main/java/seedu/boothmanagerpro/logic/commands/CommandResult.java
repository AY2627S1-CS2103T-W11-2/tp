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
    private final boolean shouldShowHelp;

    /** The application should exit. */
    private final boolean shouldExit;

    /** Contacts awaiting a numbered reply to a view request. */
    private final List<Person> viewChoices;

    /** Contact whose details should be shown, when a view selection has been resolved. */
    private final Person personToView;

    /**
     * Constructs a {@code CommandResult} with the specified fields.
     */
    public CommandResult(String feedbackToUser, boolean shouldShowHelp, boolean shouldExit) {
        this(feedbackToUser, shouldShowHelp, shouldExit, List.of(), null);
    }

    /**
     * Constructs a result with an immutable snapshot of the choices and an optional selected contact.
     */
    private CommandResult(String feedbackToUser, boolean shouldShowHelp, boolean shouldExit,
            List<Person> viewChoices, Person personToView) {
        this.personToView = personToView;
        this.viewChoices = List.copyOf(viewChoices);
        this.feedbackToUser = requireNonNull(feedbackToUser);
        this.shouldShowHelp = shouldShowHelp;
        this.shouldExit = shouldExit;
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

    /**
     * Returns whether the help window should be shown.
     */
    public boolean shouldShowHelp() {
        return shouldShowHelp;
    }

    /**
     * Returns whether the application should exit.
     */
    public boolean shouldExit() {
        return shouldExit;
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
                && shouldShowHelp == otherCommandResult.shouldShowHelp
                && shouldExit == otherCommandResult.shouldExit
                && viewChoices.equals(otherCommandResult.viewChoices)
                && Objects.equals(personToView, otherCommandResult.personToView);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedbackToUser, shouldShowHelp, shouldExit, viewChoices, personToView);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("feedbackToUser", feedbackToUser)
                .add("shouldShowHelp", shouldShowHelp)
                .add("shouldExit", shouldExit)
                .add("viewChoices", viewChoices)
                .add("personToView", personToView)
                .toString();
    }

}
