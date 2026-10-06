package seedu.boothmanagerpro.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Objects;

import seedu.boothmanagerpro.commons.core.index.Index;
import seedu.boothmanagerpro.commons.util.ToStringBuilder;
import seedu.boothmanagerpro.logic.Messages;
import seedu.boothmanagerpro.logic.commands.exceptions.CommandException;
import seedu.boothmanagerpro.model.Model;
import seedu.boothmanagerpro.model.person.Name;
import seedu.boothmanagerpro.model.person.Person;

/**
 * Deletes a person identified by name or by its displayed index from the address book.
 */
public class DeleteCommand extends Command {

    public static final String COMMAND_WORD = "delete";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Deletes the person identified by their full name. A displayed index can also be used.\n"
            + "Parameters: NAME or INDEX (index must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " Alex Yeoh";

    public static final String MESSAGE_DELETE_PERSON_SUCCESS = "Deleted person: %1$s";
    public static final String MESSAGE_PERSON_NOT_FOUND = "No contact found with the name: %1$s";
    public static final String MESSAGE_MULTIPLE_PERSONS_FOUND =
            "More than one contact has the name %1$s. Use list or find, then delete the contact by its displayed "
                    + "index.";

    private final Index targetIndex;
    private final Name targetName;

    /**
     * Creates a command that deletes the person at the specified displayed index.
     */
    public DeleteCommand(Index targetIndex) {
        this.targetIndex = requireNonNull(targetIndex);
        this.targetName = null;
    }

    /**
     * Creates a command that deletes the person with the specified full name.
     */
    public DeleteCommand(Name targetName) {
        this.targetIndex = null;
        this.targetName = requireNonNull(targetName);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Person personToDelete = targetIndex == null
                ? findPersonByName(model)
                : findPersonByIndex(model);
        model.deletePerson(personToDelete);
        return new CommandResult(String.format(MESSAGE_DELETE_PERSON_SUCCESS, Messages.format(personToDelete)));
    }

    private Person findPersonByIndex(Model model) throws CommandException {
        List<Person> lastShownList = model.getFilteredPersonList();
        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }
        return lastShownList.get(targetIndex.getZeroBased());
    }

    private Person findPersonByName(Model model) throws CommandException {
        List<Person> matches = model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getName().fullName.equalsIgnoreCase(targetName.fullName))
                .toList();

        if (matches.isEmpty()) {
            throw new CommandException(String.format(MESSAGE_PERSON_NOT_FOUND, targetName));
        }
        if (matches.size() > 1) {
            throw new CommandException(String.format(MESSAGE_MULTIPLE_PERSONS_FOUND, targetName));
        }
        return matches.get(0);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof DeleteCommand otherDeleteCommand)) {
            return false;
        }

        return Objects.equals(targetIndex, otherDeleteCommand.targetIndex)
                && Objects.equals(targetName, otherDeleteCommand.targetName);
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        return targetIndex == null
                ? builder.add("targetName", targetName).toString()
                : builder.add("targetIndex", targetIndex).toString();
    }
}
