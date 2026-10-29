package seedu.boothmanagerpro.logic.commands;

import seedu.boothmanagerpro.logic.commands.exceptions.CommandException;
import seedu.boothmanagerpro.model.Model;

/**
 * Represents a command with hidden internal logic and the ability to be executed.
 */
public abstract class Command {

    /**
     * Returns whether executing this command changes address book data that must be persisted.
     * Read-only commands leave this as {@code false}; mutating commands opt in explicitly.
     */
    public boolean modifiesAddressBook() {
        return false;
    }

    /**
     * Executes the command and returns the result message.
     *
     * @param model {@code Model} which the command should operate on.
     * @return feedback message of the operation result for display
     * @throws CommandException If an error occurs during command execution.
     */
    public abstract CommandResult execute(Model model) throws CommandException;

}
