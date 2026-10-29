package seedu.boothmanagerpro.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.boothmanagerpro.model.AddressBook;
import seedu.boothmanagerpro.model.Model;

/**
 * Clears the address book after explicit user confirmation.
 */
public class ClearCommand extends Command {

    public static final String COMMAND_WORD = "clear";
    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes every contact from the address book.\n"
            + "Format: " + COMMAND_WORD + " confirm\n"
            + "Example: " + COMMAND_WORD + " confirm";
    public static final String MESSAGE_CONFIRMATION =
            "This will delete all contacts! Type 'clear confirm' to proceed.";
    public static final String MESSAGE_SUCCESS = "All contacts have been cleared!";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.setAddressBook(new AddressBook());
        return new CommandResult(MESSAGE_SUCCESS);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClearCommand;
    }
}
